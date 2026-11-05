package com.bookloop.service;

import com.bookloop.entity.*;
import com.bookloop.payment.dto.PayoutRequest;
import com.bookloop.payment.dto.PayoutResponse;
import com.bookloop.payment.dto.SellerOnboardingRequest;
import com.bookloop.payment.dto.SellerOnboardingResponse;
import com.bookloop.payment.provider.PaymentProvider;
import com.bookloop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Service managing conditional split payouts to student sellers.
 * Enforces dispute locks, idempotency, and audit trails.
 */
@Service
@Transactional
public class PayoutService {

    private final OrderRepository orderRepository;
    private final PayoutTransactionRepository payoutRepository;
    private final CommissionRecordRepository commissionRecordRepository;
    private final SellerPaymentAccountRepository sellerAccountRepository;
    private final DisputeRepository disputeRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CommissionService commissionService;
    private final FinancialAuditService auditService;
    private final PaymentProvider paymentProvider;

    public PayoutService(OrderRepository orderRepository,
                         PayoutTransactionRepository payoutRepository,
                         CommissionRecordRepository commissionRecordRepository,
                         SellerPaymentAccountRepository sellerAccountRepository,
                         DisputeRepository disputeRepository,
                         PaymentTransactionRepository paymentTransactionRepository,
                         CommissionService commissionService,
                         FinancialAuditService auditService,
                         PaymentProvider paymentProvider) {
        this.orderRepository = orderRepository;
        this.payoutRepository = payoutRepository;
        this.commissionRecordRepository = commissionRecordRepository;
        this.sellerAccountRepository = sellerAccountRepository;
        this.disputeRepository = disputeRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.commissionService = commissionService;
        this.auditService = auditService;
        this.paymentProvider = paymentProvider;
    }

    /**
     * Triggers conditional split payout once the buyer has verified receipt of book.
     */
    public PayoutTransaction triggerConditionalPayout(Long orderId, User triggeringActor) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        // Invariant check: Order must be DELIVERED, PAID_HELD, or COMPLETED (if not yet disbursed)
        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.PAID_HELD && order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("Order #" + order.getOrderNumber() + " is not in an eligible state for payout (current: " + order.getStatus() + ").");
        }

        // Lock check: Payout is blocked while a dispute is OPEN or UNDER_REVIEW
        boolean hasActiveDispute = disputeRepository.existsByOrderIdAndStatusIn(
                orderId, List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW)
        );
        if (hasActiveDispute) {
            throw new IllegalStateException("Cannot trigger payout for order #" + order.getOrderNumber() + " because there is an active dispute open.");
        }

        // Idempotency check: prevent duplicate payouts
        List<PayoutTransaction> existingPayouts = payoutRepository.findByOrderId(orderId);
        for (PayoutTransaction p : existingPayouts) {
            if (p.getStatus() == PayoutStatus.COMPLETED || p.getStatus() == PayoutStatus.REQUESTED || p.getStatus() == PayoutStatus.PROCESSING) {
                return p; // Already in progress or completed
            }
        }

        User seller = order.getEffectiveSeller();
        if (seller == null) {
            throw new IllegalStateException("No seller found for order #" + order.getOrderNumber());
        }

        // Commission snapshot check
        CommissionRecord commissionRecord = commissionRecordRepository.findByOrderId(orderId)
                .orElseGet(() -> commissionService.createCommissionSnapshot(order, null));

        BigDecimal sellerNetAmount = commissionRecord.getSellerNetAmount();
        if (sellerNetAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Seller net amount cannot be negative.");
        }

        // Ensure seller recipient account
        SellerPaymentAccount sellerAccount = sellerAccountRepository.findBySellerIdAndProviderCode(
                seller.getId(), paymentProvider.getProviderCode()
        ).orElseGet(() -> {
            SellerOnboardingRequest onbReq = new SellerOnboardingRequest(
                    seller.getId(), seller.getEmail(), seller.getFullName(), seller.getPhone(),
                    "MB", "0987654321", seller.getFullName()
            );
            SellerOnboardingResponse onbRes = paymentProvider.registerSellerRecipient(onbReq);
            SellerPaymentAccount acc = new SellerPaymentAccount(
                    seller, paymentProvider.getProviderCode(), onbRes.getProviderRecipientId(),
                    "MB", onbRes.getAccountNumberMask(), seller.getFullName()
            );
            return sellerAccountRepository.save(acc);
        });

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String idempotencyKey = "POUT-" + dateStr + "-" + shortId;

        PayoutRequest payoutReq = new PayoutRequest(
                idempotencyKey, order.getId(), order.getOrderNumber(),
                null, sellerAccount.getProviderRecipientId(),
                sellerNetAmount, "VND", "Thanh toan tien sach BookLoop #" + order.getOrderNumber()
        );

        PayoutTransaction payoutTx = new PayoutTransaction(
                order, seller, commissionRecord, paymentProvider.getProviderCode(),
                idempotencyKey, sellerNetAmount
        );
        payoutTx = payoutRepository.save(payoutTx);

        // Call Provider
        PayoutResponse payoutRes = paymentProvider.requestConditionalPayout(payoutReq);
        payoutTx.setProviderPayoutId(payoutRes.getProviderPayoutId());

        if (payoutRes.isSuccess() && payoutRes.getStatus() == PayoutStatus.COMPLETED) {
            payoutTx.setStatus(PayoutStatus.COMPLETED);
            payoutTx.setCompletedAt(LocalDateTime.now());
            payoutRepository.save(payoutTx);

            commissionService.settleCommission(orderId);
            paymentTransactionRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId).ifPresent(tx -> {
                tx.setPaymentStatus(PaymentStatus.SETTLED);
                paymentTransactionRepository.save(tx);
            });

            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(LocalDateTime.now());
            orderRepository.save(order);

            auditService.log(
                    triggeringActor != null ? triggeringActor.getId() : null,
                    triggeringActor != null ? triggeringActor.getEmail() : "SYSTEM",
                    "PAYOUT_COMPLETED", "PayoutTransaction", payoutTx.getId(),
                    idempotencyKey, sellerNetAmount, "REQUESTED", "COMPLETED",
                    "Seller: " + seller.getEmail() + ", Net: " + sellerNetAmount, null
            );
        } else if (!payoutRes.isSuccess()) {
            payoutTx.setStatus(PayoutStatus.FAILED);
            payoutTx.setFailureReason(payoutRes.getFailureReason());
            payoutRepository.save(payoutTx);

            auditService.log(
                    triggeringActor != null ? triggeringActor.getId() : null,
                    triggeringActor != null ? triggeringActor.getEmail() : "SYSTEM",
                    "PAYOUT_FAILED", "PayoutTransaction", payoutTx.getId(),
                    idempotencyKey, sellerNetAmount, "REQUESTED", "FAILED",
                    "Reason: " + payoutRes.getFailureReason(), null
            );
        }

        return payoutTx;
    }

    /**
     * Admin retries a failed payout safely with a fresh idempotency key.
     */
    public PayoutTransaction retryFailedPayout(Long payoutId, User admin) {
        PayoutTransaction payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found with ID: " + payoutId));

        if (payout.getStatus() != PayoutStatus.FAILED) {
            throw new IllegalStateException("Only FAILED payouts can be retried (current: " + payout.getStatus() + ").");
        }

        payout.setRetryCount(payout.getRetryCount() + 1);
        String newIdempotencyKey = "POUT-RETRY-" + System.currentTimeMillis() + "-" + payout.getId();
        payout.setIdempotencyKey(newIdempotencyKey);

        SellerPaymentAccount sellerAccount = sellerAccountRepository.findBySellerIdAndProviderCode(
                payout.getSeller().getId(), paymentProvider.getProviderCode()
        ).orElseThrow(() -> new IllegalStateException("Seller has no registered payment account."));

        PayoutRequest req = new PayoutRequest(
                newIdempotencyKey, payout.getOrder().getId(), payout.getOrder().getOrderNumber(),
                null, sellerAccount.getProviderRecipientId(),
                payout.getAmount(), "VND", "Thu lai giai ngan #" + payout.getOrder().getOrderNumber()
        );

        PayoutResponse res = paymentProvider.requestConditionalPayout(req);
        payout.setProviderPayoutId(res.getProviderPayoutId());

        if (res.isSuccess() && res.getStatus() == PayoutStatus.COMPLETED) {
            payout.setStatus(PayoutStatus.COMPLETED);
            payout.setFailureReason(null);
            payout.setCompletedAt(LocalDateTime.now());
            payoutRepository.save(payout);

            commissionService.settleCommission(payout.getOrder().getId());

            Order order = payout.getOrder();
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(LocalDateTime.now());
            orderRepository.save(order);

            auditService.log(
                    admin.getId(), admin.getEmail(), "PAYOUT_RETRY_SUCCESS",
                    "PayoutTransaction", payout.getId(), newIdempotencyKey, payout.getAmount(),
                    "FAILED", "COMPLETED", "Admin retry succeeded.", null
            );
        } else {
            payout.setStatus(PayoutStatus.FAILED);
            payout.setFailureReason(res.getFailureReason());
            payoutRepository.save(payout);

            auditService.log(
                    admin.getId(), admin.getEmail(), "PAYOUT_RETRY_FAILED",
                    "PayoutTransaction", payout.getId(), newIdempotencyKey, payout.getAmount(),
                    "FAILED", "FAILED", "Reason: " + res.getFailureReason(), null
            );
        }

        return payout;
    }
}

