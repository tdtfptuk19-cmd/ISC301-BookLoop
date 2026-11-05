package com.bookloop.service;

import com.bookloop.entity.*;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.payment.dto.RefundRequest;
import com.bookloop.payment.dto.RefundResponse;
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
 * Service managing buyer dispute claims, investigation, resolution, and refund disbursements.
 */
@Service
@Transactional
public class DisputeRefundService {

    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RefundTransactionRepository refundTransactionRepository;
    private final ProductRepository productRepository;
    private final CommissionService commissionService;
    private final PayoutService payoutService;
    private final FinancialAuditService auditService;
    private final PaymentProvider paymentProvider;

    public DisputeRefundService(DisputeRepository disputeRepository,
                                OrderRepository orderRepository,
                                PaymentTransactionRepository paymentTransactionRepository,
                                RefundTransactionRepository refundTransactionRepository,
                                ProductRepository productRepository,
                                CommissionService commissionService,
                                PayoutService payoutService,
                                FinancialAuditService auditService,
                                PaymentProvider paymentProvider) {
        this.disputeRepository = disputeRepository;
        this.orderRepository = orderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.refundTransactionRepository = refundTransactionRepository;
        this.productRepository = productRepository;
        this.commissionService = commissionService;
        this.payoutService = payoutService;
        this.auditService = auditService;
        this.paymentProvider = paymentProvider;
    }

    /**
     * Buyer opens a dispute against an order (e.g. damaged book, wrong edition, non-delivery).
     * Locks conditional fund holding and prevents automatic seller payout.
     */
    public Dispute openDispute(Long orderId, User buyer, String reason, String description, String evidenceUrl) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if (!order.isBuyer(buyer.getId())) {
            throw new UnauthorizedOperationException("Only the buyer can open a dispute for order #" + order.getOrderNumber());
        }

        if (order.getStatus() != OrderStatus.PAID_HELD && order.getStatus() != OrderStatus.SHIPPED && order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Dispute can only be opened for paid orders in transit or awaiting disbursement (current: " + order.getStatus() + ").");
        }

        boolean hasActiveDispute = disputeRepository.existsByOrderIdAndStatusIn(
                orderId, List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW)
        );
        if (hasActiveDispute) {
            throw new IllegalStateException("An active dispute is already open for this order.");
        }

        Dispute dispute = new Dispute(order, buyer, reason, description, evidenceUrl);
        Dispute savedDispute = disputeRepository.save(dispute);

        order.setStatus(OrderStatus.DISPUTED);
        orderRepository.save(order);

        auditService.log(
                buyer.getId(), buyer.getEmail(), "DISPUTE_OPENED",
                "Dispute", savedDispute.getId(), order.getOrderNumber(), order.getTotalAmount(),
                order.getStatus().name(), "DISPUTED", "Reason: " + reason, null
        );

        return savedDispute;
    }

    /**
     * Admin resolves a dispute with either REFUND (return money to buyer) or RELEASE (release funds to seller).
     */
    public Dispute resolveDispute(Long disputeId, DisputeStatus resolution, String resolutionNote, User admin) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found with ID: " + disputeId));

        if (dispute.getStatus() != DisputeStatus.OPEN && dispute.getStatus() != DisputeStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Dispute is already resolved (current status: " + dispute.getStatus() + ").");
        }

        Order order = dispute.getOrder();

        if (resolution == DisputeStatus.RESOLVED_REFUND) {
            // Refund to buyer
            executeRefund(order, dispute, order.getTotalAmount(), RefundType.FULL, resolutionNote, admin);
            dispute.setStatus(DisputeStatus.RESOLVED_REFUND);
        } else if (resolution == DisputeStatus.RESOLVED_RELEASE) {
            // Dismiss dispute and release payout to seller
            dispute.setStatus(DisputeStatus.RESOLVED_RELEASE);
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);

            // Trigger payout
            payoutService.triggerConditionalPayout(order.getId(), admin);
        } else {
            throw new IllegalArgumentException("Unsupported dispute resolution: " + resolution);
        }

        dispute.setResolutionNote(resolutionNote);
        dispute.setResolvedBy(admin);
        dispute.setResolvedAt(LocalDateTime.now());
        Dispute saved = disputeRepository.save(dispute);

        auditService.log(
                admin.getId(), admin.getEmail(), "DISPUTE_RESOLVED",
                "Dispute", saved.getId(), order.getOrderNumber(), order.getTotalAmount(),
                "OPEN", resolution.name(), "Admin note: " + resolutionNote, null
        );

        return saved;
    }

    public Dispute resolveDispute(Long disputeId, DisputeStatus resolution, BigDecimal refundAmount,
                                  String resolutionNote, User admin) {
        return resolveDispute(disputeId, resolution, resolutionNote, admin);
    }

    /**
     * Executes refund via payment provider.
     */
    public RefundTransaction executeRefund(Order order, Dispute dispute, BigDecimal amount,
                                          RefundType refundType, String reason, User actor) {
        PaymentTransaction payTx = paymentTransactionRepository.findTopByOrderIdOrderByCreatedAtDesc(order.getId())
                .orElseThrow(() -> new IllegalStateException("No payment transaction found for order #" + order.getOrderNumber()));

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String idempotencyKey = "REF-" + dateStr + "-" + shortId;

        RefundRequest req = new RefundRequest(
                idempotencyKey, payTx.getProviderPaymentId(), payTx.getMerchantReference(),
                amount, refundType, reason
        );

        RefundTransaction refundTx = new RefundTransaction(
                payTx, order, dispute, idempotencyKey, amount, refundType, reason, actor
        );

        RefundResponse res = paymentProvider.requestRefund(req);
        refundTx.setProviderRefundId(res.getProviderRefundId());

        if (res.isSuccess()) {
            refundTx.setStatus(RefundStatus.COMPLETED);
            refundTx.setCompletedAt(LocalDateTime.now());
            refundTransactionRepository.save(refundTx);

            payTx.setPaymentStatus(refundType == RefundType.FULL ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);
            paymentTransactionRepository.save(payTx);

            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);

            commissionService.reverseCommission(order.getId(), reason);

            // Restore products to AVAILABLE
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Product p = item.getProduct();
                    p.setStatus(ListingStatus.AVAILABLE);
                    productRepository.save(p);
                }
            }

            auditService.log(
                    actor != null ? actor.getId() : null,
                    actor != null ? actor.getEmail() : "SYSTEM",
                    "REFUND_COMPLETED", "RefundTransaction", refundTx.getId(),
                    idempotencyKey, amount, "PAID_HELD", "REFUNDED", reason, null
            );
        } else {
            refundTx.setStatus(RefundStatus.FAILED);
            refundTransactionRepository.save(refundTx);

            auditService.log(
                    actor != null ? actor.getId() : null,
                    actor != null ? actor.getEmail() : "SYSTEM",
                    "REFUND_FAILED", "RefundTransaction", refundTx.getId(),
                    idempotencyKey, amount, "REQUESTED", "FAILED", res.getFailureReason(), null
            );
        }

        return refundTx;
    }
}

