package com.bookloop.service;

import com.bookloop.entity.*;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.payment.dto.PaymentSessionRequest;
import com.bookloop.payment.dto.PaymentSessionResponse;
import com.bookloop.payment.dto.WebhookVerificationResult;
import com.bookloop.payment.provider.MockPaymentProvider;
import com.bookloop.payment.provider.PaymentProvider;
import com.bookloop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrator service managing the complete payment lifecycle: session creation,
 * webhook processing, partner escrow holding status, and split settlements.
 */
@Service
@Transactional
public class PaymentManagerService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final PayoutTransactionRepository payoutTransactionRepository;
    private final RefundTransactionRepository refundTransactionRepository;
    private final ProductRepository productRepository;
    private final CommissionService commissionService;
    private final FinancialAuditService auditService;
    private final PaymentProvider paymentProvider;

    public PaymentManagerService(OrderRepository orderRepository,
                                 PaymentTransactionRepository paymentTransactionRepository,
                                 PaymentWebhookEventRepository webhookEventRepository,
                                 PayoutTransactionRepository payoutTransactionRepository,
                                 RefundTransactionRepository refundTransactionRepository,
                                 ProductRepository productRepository,
                                 CommissionService commissionService,
                                 FinancialAuditService auditService,
                                 PaymentProvider paymentProvider) {
        this.orderRepository = orderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.payoutTransactionRepository = payoutTransactionRepository;
        this.refundTransactionRepository = refundTransactionRepository;
        this.productRepository = productRepository;
        this.commissionService = commissionService;
        this.auditService = auditService;
        this.paymentProvider = paymentProvider;
    }

    public PaymentSessionResponse initiateCheckoutSession(Order order, String returnUrl, String cancelUrl) {
        return createPaymentSession(order.getId(), order.getBuyer());
    }

    /**
     * Initiates a payment session with the payment provider for an order.
     */
    public PaymentSessionResponse createPaymentSession(Long orderId, User buyer) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!order.isBuyer(buyer.getId())) {
            throw new UnauthorizedOperationException("Only the buyer can initiate payment for order #" + order.getOrderNumber());
        }

        if (order.getStatus() == OrderStatus.PAID_HELD || order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Order #" + order.getOrderNumber() + " is already paid.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for a cancelled order.");
        }

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String merchantRef = "PAY-" + dateStr + "-" + shortId;

        PaymentSessionRequest req = new PaymentSessionRequest(
                merchantRef, order.getId(), order.getTotalAmount(), "VND",
                "Thanh toan giao trinh BookLoop #" + order.getOrderNumber(),
                buyer.getEmail(), buyer.getFullName(),
                "/orders/" + order.getId()
        );

        PaymentSessionResponse sessionRes = paymentProvider.createPaymentSession(req);
        if (!sessionRes.isSuccess()) {
            throw new IllegalStateException("Payment provider error: " + sessionRes.getErrorMessage());
        }

        PaymentTransaction payTx = new PaymentTransaction();
        payTx.setOrder(order);
        payTx.setProviderCode(paymentProvider.getProviderCode());
        payTx.setProviderPaymentId(sessionRes.getProviderPaymentId());
        payTx.setMerchantReference(merchantRef);
        payTx.setAmount(order.getTotalAmount());
        payTx.setCurrency("VND");
        payTx.setPaymentStatus(PaymentStatus.PENDING);
        payTx.setPaymentMethodType("QR_CODE");
        payTx.setQrCodeData(sessionRes.getQrCodeData());
        payTx.setPaymentUrl(sessionRes.getPaymentUrl());
        PaymentTransaction savedTx = paymentTransactionRepository.save(payTx);

        // Update order status to AWAITING_PAYMENT if still PENDING
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.AWAITING_PAYMENT);
            orderRepository.save(order);
        }

        auditService.log(
                buyer.getId(), buyer.getEmail(), "PAYMENT_SESSION_CREATED",
                "PaymentTransaction", savedTx.getId(), merchantRef, order.getTotalAmount(),
                null, "PENDING", "Provider: " + paymentProvider.getProviderCode(), null
        );

        return sessionRes;
    }

    /**
     * Processes incoming webhook from payment provider with signature verification and idempotency.
     */
    public boolean processWebhook(String providerCode, String rawPayload, Map<String, String> headers) {
        WebhookVerificationResult result = paymentProvider.verifyWebhook(rawPayload, headers);

        PaymentWebhookEvent event = new PaymentWebhookEvent(
                providerCode,
                result.getProviderEventId() != null ? result.getProviderEventId() : "evt_" + UUID.randomUUID().toString().substring(0, 8),
                result.getEventType() != null ? result.getEventType() : "unknown",
                result.isValid(),
                rawPayload
        );

        if (!result.isValid()) {
            event.setProcessingStatus(WebhookProcessingStatus.FAILED);
            event.setErrorMessage(result.getFailureReason());
            webhookEventRepository.save(event);
            return false;
        }

        // Idempotency check: if this event was already processed, ignore
        if (webhookEventRepository.existsByProviderCodeAndProviderEventId(providerCode, result.getProviderEventId())) {
            event.setProcessingStatus(WebhookProcessingStatus.IGNORED);
            event.setErrorMessage("Duplicate webhook event ignored.");
            webhookEventRepository.save(event);
            return true;
        }

        event.setProcessingStatus(WebhookProcessingStatus.PROCESSED);
        event.setProcessedAt(LocalDateTime.now());
        webhookEventRepository.save(event);

        // Handle Event Types
        String eventType = result.getEventType();
        if ("payment.paid_held".equalsIgnoreCase(eventType) || "payment.success".equalsIgnoreCase(eventType)) {
            handlePaymentPaidHeld(result.getMerchantReference(), result.getProviderPaymentId(), result.getAmount());
        } else if ("payout.completed".equalsIgnoreCase(eventType)) {
            handlePayoutCompleted(result.getMerchantReference());
        } else if ("refund.completed".equalsIgnoreCase(eventType)) {
            handleRefundCompleted(result.getMerchantReference());
        }

        return true;
    }

    /**
     * Handles payment confirmation: transitions payment to PAID_HELD (partner holding escrow).
     */
    public void handlePaymentPaidHeld(String merchantReference, String providerPaymentId, java.math.BigDecimal amount) {
        PaymentTransaction payTx = paymentTransactionRepository.findByMerchantReference(merchantReference)
                .or(() -> paymentTransactionRepository.findByProviderPaymentId(providerPaymentId))
                .orElse(null);

        if (payTx == null) {
            return;
        }

        if (payTx.getPaymentStatus() == PaymentStatus.PAID_HELD) {
            return; // Idempotent
        }

        payTx.setPaymentStatus(PaymentStatus.PAID_HELD);
        payTx.setPaidAt(LocalDateTime.now());
        paymentTransactionRepository.save(payTx);

        Order order = payTx.getOrder();
        order.setStatus(OrderStatus.PAID_HELD);
        orderRepository.save(order);

        // Create immutable commission record snapshot
        commissionService.createCommissionSnapshot(order, payTx);

        auditService.log(
                null, "WEBHOOK", "PAYMENT_PAID_HELD",
                "PaymentTransaction", payTx.getId(), payTx.getMerchantReference(), payTx.getAmount(),
                "PENDING", "PAID_HELD", "Funds held conditionally by partner.", null
        );
    }

    private void handlePayoutCompleted(String providerPayoutId) {
        payoutTransactionRepository.findByProviderPayoutId(providerPayoutId).ifPresent(payout -> {
            if (payout.getStatus() == PayoutStatus.COMPLETED) {
                return;
            }
            payout.setStatus(PayoutStatus.COMPLETED);
            payout.setCompletedAt(LocalDateTime.now());
            payoutTransactionRepository.save(payout);

            commissionService.settleCommission(payout.getOrder().getId());

            Order order = payout.getOrder();
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(LocalDateTime.now());
            orderRepository.save(order);

            auditService.log(
                    null, "WEBHOOK", "PAYOUT_COMPLETED",
                    "PayoutTransaction", payout.getId(), payout.getIdempotencyKey(), payout.getAmount(),
                    "REQUESTED", "COMPLETED", "Split payout delivered to seller.", null
            );
        });
    }

    private void handleRefundCompleted(String providerRefundId) {
        refundTransactionRepository.findByIdempotencyKey(providerRefundId).ifPresent(refund -> {
            if (refund.getStatus() == RefundStatus.COMPLETED) {
                return;
            }
            refund.setStatus(RefundStatus.COMPLETED);
            refund.setCompletedAt(LocalDateTime.now());
            refundTransactionRepository.save(refund);

            Order order = refund.getOrder();
            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);

            commissionService.reverseCommission(order.getId(), "Refunded to buyer");

            // Restore book items
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Product p = item.getProduct();
                    p.setStatus(ListingStatus.AVAILABLE);
                    productRepository.save(p);
                }
            }

            auditService.log(
                    null, "WEBHOOK", "REFUND_COMPLETED",
                    "RefundTransaction", refund.getId(), refund.getIdempotencyKey(), refund.getRefundAmount(),
                    "REQUESTED", "COMPLETED", "Refund returned to buyer.", null
            );
        });
    }

    /**
     * Sandbox simulation helper: simulates a successful buyer payment via Mock Provider.
     */
    public void simulateBuyerPaymentSuccess(String merchantReference) {
        PaymentTransaction payTx = paymentTransactionRepository.findByMerchantReference(merchantReference)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + merchantReference));

        if (paymentProvider instanceof MockPaymentProvider mockProvider) {
            mockProvider.markTransactionPaid(merchantReference);
        }

        handlePaymentPaidHeld(merchantReference, payTx.getProviderPaymentId(), payTx.getAmount());
    }
}

