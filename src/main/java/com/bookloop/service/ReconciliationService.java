package com.bookloop.service;

import com.bookloop.dto.ReconciliationItemDto;
import com.bookloop.dto.ReconciliationReportDto;
import com.bookloop.entity.*;
import com.bookloop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service performing reconciliation between internal order balances, holding transactions,
 * commission deductions, seller payouts, refunds, and mock payment gateway records.
 */
@Service
@Transactional
public class ReconciliationService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final PayoutTransactionRepository payoutRepository;
    private final CommissionRecordRepository commissionRepository;
    private final RefundTransactionRepository refundRepository;
    private final FinancialAuditService auditService;

    public ReconciliationService(OrderRepository orderRepository,
                                 PaymentTransactionRepository paymentRepository,
                                 PayoutTransactionRepository payoutRepository,
                                 CommissionRecordRepository commissionRepository,
                                 RefundTransactionRepository refundRepository,
                                 FinancialAuditService auditService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.payoutRepository = payoutRepository;
        this.commissionRepository = commissionRepository;
        this.refundRepository = refundRepository;
        this.auditService = auditService;
    }

    public ReconciliationReportDto runReconciliation(String auditorName) {
        return runReconciliation(auditorName, true);
    }

    public ReconciliationReportDto runReconciliation(String auditorName, boolean recordAudit) {
        ReconciliationReportDto report = new ReconciliationReportDto();

        List<Order> allOrders = orderRepository.findAll();
        List<PaymentTransaction> allPayments = paymentRepository.findAll();
        List<PayoutTransaction> allPayouts = payoutRepository.findAll();

        report.setTotalOrdersChecked(allOrders.size());
        report.setTotalPaymentsChecked(allPayments.size());
        report.setTotalPayoutsChecked(allPayouts.size());

        BigDecimal calculatedEscrow = BigDecimal.ZERO;

        for (Order order : allOrders) {
            ReconciliationItemDto item = new ReconciliationItemDto();
            item.setOrderId(order.getId());
            item.setOrderNumber(order.getOrderNumber());
            item.setOrderGrossAmount(order.getTotalAmount());
            item.setTransactionDate(order.getOrderDate());
            item.setMockGateway(true);

            // Match PaymentTransaction
            Optional<PaymentTransaction> paymentOpt = paymentRepository.findTopByOrderIdOrderByCreatedAtDesc(order.getId());
            if (paymentOpt.isPresent()) {
                PaymentTransaction payment = paymentOpt.get();
                item.setPaymentReference(payment.getMerchantReference());
                item.setProviderReference(payment.getProviderPaymentId() != null ? payment.getProviderPaymentId() : "MOCK-TXN-" + payment.getId());
                item.setPaymentMethod(payment.getPaymentMethodType() != null ? payment.getPaymentMethodType() : (order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "MARKETPLACE_HOLDING"));
                item.setPaymentAmount(payment.getAmount());
            } else {
                item.setPaymentReference("N/A");
                item.setProviderReference("NO-MOCK-TXN");
                item.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD_MEETUP");
                item.setPaymentAmount(BigDecimal.ZERO);
            }

            // Match CommissionRecord
            Optional<CommissionRecord> commOpt = commissionRepository.findByOrderId(order.getId());
            commOpt.ifPresent(c -> item.setCommissionAmount(c.getCommissionAmount()));

            // Match PayoutTransaction
            List<PayoutTransaction> payouts = payoutRepository.findByOrderId(order.getId());
            if (!payouts.isEmpty()) {
                BigDecimal sumPayout = payouts.stream()
                        .filter(p -> p.getStatus() == PayoutStatus.PAID || p.getStatus() == PayoutStatus.CONFIRMED || p.getStatus() == PayoutStatus.COMPLETED)
                        .map(PayoutTransaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                item.setPayoutAmount(sumPayout);
            }

            // Match RefundTransaction
            List<RefundTransaction> refunds = refundRepository.findByOrderId(order.getId());
            if (!refunds.isEmpty()) {
                BigDecimal sumRefund = refunds.stream()
                        .filter(r -> r.getStatus() == RefundStatus.REFUNDED || r.getStatus() == RefundStatus.CONFIRMED)
                        .map(RefundTransaction::getRefundAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                item.setRefundAmount(sumRefund);
            }

            // Evaluate Business Invariants & Classification
            if (order.getStatus() == OrderStatus.CANCELLED) {
                if (item.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
                    item.setStatus(ReconciliationStatus.MATCHED);
                    item.setNotes("Đơn đã hủy & hoàn trả người mua thành công (" + item.getRefundAmount() + " ₫)");
                } else if (item.getPaymentAmount().compareTo(BigDecimal.ZERO) == 0) {
                    item.setStatus(ReconciliationStatus.MATCHED);
                    item.setNotes("Đơn đã hủy trước khi thanh toán, không phát sinh dòng tiền");
                } else {
                    item.setStatus(ReconciliationStatus.MISMATCH);
                    item.setDiscrepancyAmount(item.getPaymentAmount());
                    item.setNotes("Đơn hủy nhưng chưa ghi nhận giao dịch hoàn tiền cổng!");
                    report.addDiscrepancy("UNREFUNDED_CANCELLED_ORDER", order.getOrderNumber(),
                            "Đơn đã hủy nhưng chưa ghi nhận hoàn tiền " + item.getPaymentAmount() + " ₫", "HIGH");
                }
            } else if (order.getStatus() == OrderStatus.PENDING) {
                item.setStatus(ReconciliationStatus.PENDING);
                item.setNotes("Đang chờ người mua hoàn tất thanh toán");
            } else if (order.getStatus() == OrderStatus.PAID_HELD || order.getStatus() == OrderStatus.SHIPPED) {
                calculatedEscrow = calculatedEscrow.add(order.getTotalAmount());
                if (paymentOpt.isPresent()) {
                    PaymentTransaction pt = paymentOpt.get();
                    if (pt.getPaymentStatus() == PaymentStatus.HELD && pt.getAmount().compareTo(order.getTotalAmount()) == 0) {
                        item.setStatus(ReconciliationStatus.PENDING);
                        item.setNotes("Tiền thanh toán đang được giữ bảo đảm an toàn (Escrow)");
                    } else if (pt.getAmount().compareTo(order.getTotalAmount()) != 0) {
                        item.setStatus(ReconciliationStatus.MISMATCH);
                        BigDecimal diff = order.getTotalAmount().subtract(pt.getAmount());
                        item.setDiscrepancyAmount(diff);
                        item.setNotes("Số tiền người mua thanh toán (" + pt.getAmount() + " ₫) lệch với đơn hàng (" + order.getTotalAmount() + " ₫)");
                        report.addDiscrepancy("ESCROW_AMOUNT_MISMATCH", order.getOrderNumber(),
                                "Số tiền tạm giữ khác giá trị đơn hàng: lệch " + diff + " ₫", "HIGH");
                    } else {
                        item.setStatus(ReconciliationStatus.PENDING);
                        item.setNotes("Giao dịch tạm giữ trạng thái: " + pt.getPaymentStatus().name());
                    }
                } else {
                    item.setStatus(ReconciliationStatus.MISSING_PROVIDER_DATA);
                    item.setNotes("Đơn hàng ghi nhận PAID_HELD nhưng thiếu bản ghi thanh toán cổng đối soát!");
                    report.addDiscrepancy("MISSING_PAYMENT_TX", order.getOrderNumber(),
                            "Thiếu dữ liệu cổng thanh toán cho đơn hàng đã giữ tiền", "HIGH");
                }
            } else if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.DELIVERED) {
                if (paymentOpt.isEmpty()) {
                    item.setStatus(ReconciliationStatus.MISSING_PROVIDER_DATA);
                    item.setNotes("Đơn đã giao/hoàn tất nhưng không tìm thấy giao dịch thanh toán cổng");
                    report.addDiscrepancy("MISSING_PAYMENT_DATA", order.getOrderNumber(),
                            "Không tìm thấy giao dịch thanh toán cổng", "HIGH");
                } else if (!payouts.isEmpty() && commOpt.isPresent()) {
                    PayoutTransaction payout = payouts.get(0);
                    CommissionRecord comm = commOpt.get();

                    BigDecimal sellerNet = payout.getAmount();
                    BigDecimal platformComm = comm.getCommissionAmount();
                    BigDecimal gatewayFee = comm.getGatewayFeeSnapshot() != null ? comm.getGatewayFeeSnapshot() : BigDecimal.ZERO;
                    BigDecimal splitTotal = sellerNet.add(platformComm).add(gatewayFee);

                    BigDecimal diff = order.getTotalAmount().subtract(splitTotal);
                    item.setDiscrepancyAmount(diff);

                    if (diff.compareTo(BigDecimal.ZERO) == 0) {
                        item.setStatus(ReconciliationStatus.MATCHED);
                        item.setNotes("Khớp hoàn hảo 100%: Giá trị đơn (" + order.getTotalAmount() + " ₫) = Giải ngân (" + sellerNet + " ₫) + Hoa hồng (" + platformComm + " ₫)");
                    } else {
                        item.setStatus(ReconciliationStatus.MISMATCH);
                        item.setNotes("Sai lệch công thức: Đơn (" + order.getTotalAmount() + " ₫) != Giải ngân + Hoa hồng (" + splitTotal + " ₫), chênh lệch: " + diff + " ₫");
                        report.addDiscrepancy("GROSS_SPLIT_MISMATCH", order.getOrderNumber(),
                                String.format("Order gross %s != Payout %s + Commission %s (chênh %s ₫)",
                                        order.getTotalAmount(), sellerNet, platformComm, diff), "HIGH");
                    }
                } else if (payouts.isEmpty()) {
                    item.setStatus(ReconciliationStatus.PENDING);
                    item.setNotes("Đơn đã giao/hoàn tất. Doanh số đã sẵn sàng chờ người bán gửi yêu cầu giải ngân");
                } else {
                    item.setStatus(ReconciliationStatus.MISSING_INTERNAL_DATA);
                    item.setNotes("Thiếu bản ghi hoa hồng đối soát nội bộ");
                    report.addDiscrepancy("MISSING_COMMISSION_RECORD", order.getOrderNumber(),
                            "Đơn hoàn tất có lệnh giải ngân nhưng thiếu bản ghi hoa hồng sàn", "MEDIUM");
                }
            } else if (order.getStatus() == OrderStatus.REFUNDED) {
                if (!refunds.isEmpty()) {
                    item.setStatus(ReconciliationStatus.MATCHED);
                    item.setNotes("Đã hoàn tiền cho người mua (" + item.getRefundAmount() + " ₫)");
                } else {
                    item.setStatus(ReconciliationStatus.MISSING_INTERNAL_DATA);
                    item.setNotes("Đơn đánh dấu REFUNDED nhưng thiếu bản ghi RefundTransaction!");
                    report.addDiscrepancy("REFUND_RECORD_MISSING", order.getOrderNumber(),
                            "Đơn hoàn tiền thiếu bản ghi đối soát hoàn tiền", "HIGH");
                }
            } else {
                item.setStatus(ReconciliationStatus.PENDING);
                item.setNotes("Trạng thái đơn: " + order.getStatus().name());
            }

            // Check Duplicate Payout Invariant
            List<PayoutTransaction> successfulPayouts = payouts.stream()
                    .filter(p -> p.getStatus() == PayoutStatus.PAID || p.getStatus() == PayoutStatus.CONFIRMED || p.getStatus() == PayoutStatus.COMPLETED)
                    .toList();
            if (successfulPayouts.size() > 1) {
                item.setStatus(ReconciliationStatus.ERROR);
                item.setNotes("CẢNH BÁO: Phát hiện " + successfulPayouts.size() + " lệnh chi trả thành công cho cùng 1 đơn hàng!");
                report.addDiscrepancy("DUPLICATE_PAYOUT", order.getOrderNumber(),
                        "Order has " + successfulPayouts.size() + " paid payout records!", "HIGH");
            }

            report.addItem(item);
        }

        // Check Expected Escrow vs Calculated Escrow
        BigDecimal heldPayments = paymentRepository.sumAmountByPaymentStatus(PaymentStatus.HELD);
        report.setExpectedEscrowBalance(heldPayments != null ? heldPayments : BigDecimal.ZERO);
        report.setCalculatedEscrowBalance(calculatedEscrow);

        if (report.getExpectedEscrowBalance().compareTo(report.getCalculatedEscrowBalance()) != 0) {
            report.setEscrowBalanced(false);
            report.addDiscrepancy(
                    "ESCROW_HOLDING_DIFF",
                    "LEDGER",
                    String.format("Tổng giao dịch HELD (%s ₫) khác tổng các đơn hàng đang giao (%s ₫)",
                            report.getExpectedEscrowBalance(), report.getCalculatedEscrowBalance()),
                    "MEDIUM"
            );
        }

        if (recordAudit) {
            auditService.logEvent(
                    "RECONCILIATION_RUN",
                    "ReconciliationReport",
                    null,
                    auditorName,
                    String.format("Reconciliation run completed: %d discrepancies, %d matched, %d pending across %d orders",
                            report.getDiscrepancyCount(), report.getMatchedCount(), report.getPendingCount(), report.getTotalOrdersChecked()),
                    null,
                    "Discrepancies: " + report.getDiscrepancyCount()
            );
        }

        return report;
    }
}
