package com.bookloop.service;

import com.bookloop.dto.AdminFinanceDashboardDto;
import com.bookloop.entity.*;
import com.bookloop.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service providing administrative oversight, metrics, and intervention capabilities
 * across all marketplace financial transactions.
 */
@Service
@Transactional
public class AdminFinanceService {

    private final PaymentTransactionRepository paymentRepository;
    private final PayoutTransactionRepository payoutRepository;
    private final CommissionRecordRepository commissionRepository;
    private final RefundTransactionRepository refundRepository;
    private final DisputeRepository disputeRepository;
    private final FinancialAuditLogRepository auditRepository;
    private final CommissionSettingRepository commissionSettingRepository;
    private final OrderRepository orderRepository;
    private final PayoutService payoutService;
    private final DisputeRefundService disputeRefundService;
    private final FinancialAuditService auditService;

    public AdminFinanceService(PaymentTransactionRepository paymentRepository,
                               PayoutTransactionRepository payoutRepository,
                               CommissionRecordRepository commissionRepository,
                               RefundTransactionRepository refundRepository,
                               DisputeRepository disputeRepository,
                               FinancialAuditLogRepository auditRepository,
                               CommissionSettingRepository commissionSettingRepository,
                               OrderRepository orderRepository,
                               PayoutService payoutService,
                               DisputeRefundService disputeRefundService,
                               FinancialAuditService auditService) {
        this.paymentRepository = paymentRepository;
        this.payoutRepository = payoutRepository;
        this.commissionRepository = commissionRepository;
        this.refundRepository = refundRepository;
        this.disputeRepository = disputeRepository;
        this.auditRepository = auditRepository;
        this.commissionSettingRepository = commissionSettingRepository;
        this.orderRepository = orderRepository;
        this.payoutService = payoutService;
        this.disputeRefundService = disputeRefundService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public AdminFinanceDashboardDto getDashboardKpis() {
        return getDashboardKpis("ALL", null, null);
    }

    @Transactional(readOnly = true)
    public AdminFinanceDashboardDto getDashboardKpis(String range, LocalDate customStart, LocalDate customEnd) {
        AdminFinanceDashboardDto dto = new AdminFinanceDashboardDto();
        String selectedRange = (range != null && !range.isBlank()) ? range.trim().toUpperCase() : "ALL";
        dto.setSelectedRange(selectedRange);

        LocalDateTime from = null;
        LocalDateTime to = null;
        LocalDate today = LocalDate.now();

        switch (selectedRange) {
            case "TODAY":
                from = today.atStartOfDay();
                to = today.atTime(LocalTime.MAX);
                break;
            case "LAST_7_DAYS":
                from = today.minusDays(7).atStartOfDay();
                to = today.atTime(LocalTime.MAX);
                break;
            case "LAST_30_DAYS":
                from = today.minusDays(30).atStartOfDay();
                to = today.atTime(LocalTime.MAX);
                break;
            case "THIS_MONTH":
                from = today.withDayOfMonth(1).atStartOfDay();
                to = today.atTime(LocalTime.MAX);
                break;
            case "LAST_MONTH":
                LocalDate prevMonth = today.minusMonths(1);
                from = prevMonth.withDayOfMonth(1).atStartOfDay();
                to = prevMonth.withDayOfMonth(prevMonth.lengthOfMonth()).atTime(LocalTime.MAX);
                break;
            case "CUSTOM":
                if (customStart != null) from = customStart.atStartOfDay();
                if (customEnd != null) to = customEnd.atTime(LocalTime.MAX);
                break;
            case "ALL":
            default:
                from = null;
                to = null;
                selectedRange = "ALL";
                dto.setSelectedRange(selectedRange);
                break;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (from != null) dto.setStartDate(from.toLocalDate().format(dtf));
        if (to != null) dto.setEndDate(to.toLocalDate().format(dtf));

        // 1. GMV: Tổng giá trị hàng hóa của giao dịch thanh toán đủ điều kiện (HELD, SETTLED, PARTIALLY_REFUNDED)
        BigDecimal gmv = paymentRepository.sumAmountByStatusesAndDateRange(
                List.of(PaymentStatus.HELD, PaymentStatus.SETTLED, PaymentStatus.PARTIALLY_REFUNDED),
                from, to);
        dto.setTotalGmv(gmv != null ? gmv.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // 2. Platform Revenue: Doanh thu hoa hồng BookLoop (SETTLED) - Không tính đúp, không cộng Held Funds
        BigDecimal commRevenue = commissionRepository.sumCommissionAmountByStatusAndDateRange(
                CommissionStatus.SETTLED, from, to);
        dto.setTotalPlatformCommission(commRevenue != null ? commRevenue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // 3. Held Funds: Tiền đang được đối tác/sàn tạm giữ đối soát (Escrow)
        BigDecimal heldFunds = paymentRepository.sumAmountByStatusAndDateRange(PaymentStatus.HELD, from, to);
        dto.setTotalHeldEscrow(heldFunds != null ? heldFunds.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // 4. Seller Payout: Tiền đã giải ngân cho người bán (PAID, CONFIRMED, COMPLETED)
        BigDecimal sellerPayout = payoutRepository.sumAmountByStatusesAndDateRange(
                List.of(PayoutStatus.PAID, PayoutStatus.CONFIRMED, PayoutStatus.COMPLETED),
                from, to);
        dto.setTotalDisbursedPayouts(sellerPayout != null ? sellerPayout.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // 5. Successful Payments: Giao dịch thanh toán thành công (count & amount)
        long succCount = paymentRepository.countByStatusesAndDateRange(
                List.of(PaymentStatus.HELD, PaymentStatus.SETTLED, PaymentStatus.PARTIALLY_REFUNDED),
                from, to);
        dto.setTotalSuccessfulPayments(succCount);
        dto.setTotalSuccessfulPaymentsAmount(dto.getTotalGmv());

        // 6. Pending Payments: Giao dịch đang chờ thanh toán (count & amount)
        long pendingCount = paymentRepository.countByStatusAndDateRange(PaymentStatus.PENDING, from, to);
        BigDecimal pendingAmt = paymentRepository.sumAmountByStatusAndDateRange(PaymentStatus.PENDING, from, to);
        dto.setTotalPendingPayments(pendingCount);
        dto.setTotalPendingPaymentsAmount(pendingAmt != null ? pendingAmt.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // 7. Refunds: Khoản tiền hoàn trả (count & amount)
        BigDecimal refundsAmt = refundRepository.sumAmountByStatusesAndDateRange(
                List.of(RefundStatus.REFUNDED, RefundStatus.CONFIRMED),
                from, to);
        long refundsCount = refundRepository.countByStatusesAndDateRange(
                List.of(RefundStatus.REFUNDED, RefundStatus.CONFIRMED),
                from, to);
        dto.setTotalRefunded(refundsAmt != null ? refundsAmt.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        dto.setTotalRefundsCount(refundsCount);

        // 8. Disputes: Tranh chấp (active count & total count)
        long activeDisputes = disputeRepository.countByStatusesAndDateRange(
                List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW),
                from, to);
        long totalDisputes = disputeRepository.countByStatusesAndDateRange(null, from, to);
        dto.setActiveDisputesCount(activeDisputes);
        dto.setTotalDisputesCount(totalDisputes);

        // General counters
        dto.setPendingPayoutsCount(payoutRepository.countByStatus(PayoutStatus.PENDING)
                + payoutRepository.countByStatus(PayoutStatus.PROCESSING));
        dto.setTotalOrdersCount(orderRepository.count());

        return dto;
    }

    @Transactional(readOnly = true)
    public Page<PaymentTransaction> getPayments(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findAllWithOrder(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PayoutTransaction> getPayouts(PayoutStatus status, Pageable pageable) {
        return payoutRepository.findAllWithDetails(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CommissionRecord> getCommissions(CommissionStatus status, Pageable pageable) {
        return commissionRepository.findAllWithDetails(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<RefundTransaction> getRefunds(RefundStatus status, Pageable pageable) {
        return refundRepository.findAllWithDetails(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Dispute> getDisputes(DisputeStatus status, Pageable pageable) {
        return disputeRepository.findAllWithDetails(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<FinancialAuditLog> getAuditLogs(Pageable pageable) {
        return auditRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional
    public CommissionSetting getActiveCommissionSetting() {
        return commissionSettingRepository.findTopByActiveTrueOrderByEffectiveFromDesc()
                .orElseGet(() -> {
                    CommissionSetting defaultSetting = new CommissionSetting();
                    defaultSetting.setRatePercentage(new BigDecimal("5.00"));
                    defaultSetting.setFixedFee(BigDecimal.ZERO);
                    defaultSetting.setEffectiveFrom(LocalDateTime.now());
                    defaultSetting.setActive(true);
                    return commissionSettingRepository.save(defaultSetting);
                });
    }

    public CommissionSetting updateCommissionSetting(BigDecimal ratePercentage, BigDecimal fixedFee, String adminEmail) {
        List<CommissionSetting> currentActives = commissionSettingRepository.findAll().stream()
                .filter(CommissionSetting::isActive)
                .toList();
        for (CommissionSetting s : currentActives) {
            s.setActive(false);
            commissionSettingRepository.save(s);
        }

        CommissionSetting newSetting = new CommissionSetting();
        newSetting.setRatePercentage(ratePercentage);
        newSetting.setFixedFee(fixedFee != null ? fixedFee : BigDecimal.ZERO);
        newSetting.setEffectiveFrom(LocalDateTime.now());
        newSetting.setActive(true);
        CommissionSetting saved = commissionSettingRepository.save(newSetting);

        auditService.logEvent(
                "COMMISSION_SETTING_UPDATED",
                "CommissionSetting",
                saved.getId(),
                adminEmail,
                String.format("Commission updated to %s%% + %s VND", ratePercentage, fixedFee),
                null,
                "New Rate: " + ratePercentage + "%"
        );

        return saved;
    }

    public PayoutTransaction retryPayout(Long payoutId, User adminUser) {
        PayoutTransaction payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lệnh chi trả ID: " + payoutId));

        if (payout.getStatus() == PayoutStatus.PAID || payout.getStatus() == PayoutStatus.CONFIRMED || payout.getStatus() == PayoutStatus.COMPLETED) {
            throw new IllegalStateException("Lệnh chi trả đã hoàn tất thành công, không thể gửi lại!");
        }

        return payoutService.triggerConditionalPayout(payout.getOrder().getId(), adminUser);
    }

    public Dispute resolveDispute(Long disputeId, DisputeStatus resolution, BigDecimal refundAmount, String adminNotes, User adminUser) {
        return disputeRefundService.resolveDispute(disputeId, resolution, refundAmount, adminNotes, adminUser);
    }
}
