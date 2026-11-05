package com.bookloop.dto;

import java.math.BigDecimal;

/**
 * DTO carrying financial KPI metrics for the Admin Finance Overview dashboard.
 */
public class AdminFinanceDashboardDto {

    // 1. GMV: Tổng giá trị hàng hóa của giao dịch đủ điều kiện
    private BigDecimal totalGmv;

    // 2. Platform Revenue: Doanh thu hoa hồng BookLoop
    private BigDecimal totalPlatformCommission;

    // 3. Held Funds: Tiền đang được đối tác thanh toán giữ (Escrow)
    private BigDecimal totalHeldEscrow;

    // 4. Seller Payout: Tiền đã giải ngân cho người bán
    private BigDecimal totalDisbursedPayouts;

    // 5. Successful Payments: Giao dịch thanh toán thành công
    private long totalSuccessfulPayments;
    private BigDecimal totalSuccessfulPaymentsAmount;

    // 6. Pending Payments: Giao dịch đang chờ thanh toán
    private long totalPendingPayments;
    private BigDecimal totalPendingPaymentsAmount;

    // 7. Refunds: Khoản tiền hoàn trả
    private BigDecimal totalRefunded;
    private long totalRefundsCount;

    // 8. Disputes: Giao dịch đang tranh chấp
    private long activeDisputesCount;
    private long totalDisputesCount;

    // General counters
    private long pendingPayoutsCount;
    private long totalOrdersCount;

    // Time filter info
    private String selectedRange;
    private String startDate;
    private String endDate;

    public AdminFinanceDashboardDto() {
        this.totalGmv = BigDecimal.ZERO;
        this.totalPlatformCommission = BigDecimal.ZERO;
        this.totalHeldEscrow = BigDecimal.ZERO;
        this.totalDisbursedPayouts = BigDecimal.ZERO;
        this.totalSuccessfulPayments = 0;
        this.totalSuccessfulPaymentsAmount = BigDecimal.ZERO;
        this.totalPendingPayments = 0;
        this.totalPendingPaymentsAmount = BigDecimal.ZERO;
        this.totalRefunded = BigDecimal.ZERO;
        this.totalRefundsCount = 0;
        this.activeDisputesCount = 0;
        this.totalDisputesCount = 0;
        this.pendingPayoutsCount = 0;
        this.totalOrdersCount = 0;
        this.selectedRange = "ALL";
    }

    public BigDecimal getTotalGmv() {
        return totalGmv != null ? totalGmv : BigDecimal.ZERO;
    }

    public void setTotalGmv(BigDecimal totalGmv) {
        this.totalGmv = totalGmv != null ? totalGmv : BigDecimal.ZERO;
    }

    public BigDecimal getTotalPlatformCommission() {
        return totalPlatformCommission != null ? totalPlatformCommission : BigDecimal.ZERO;
    }

    public void setTotalPlatformCommission(BigDecimal totalPlatformCommission) {
        this.totalPlatformCommission = totalPlatformCommission != null ? totalPlatformCommission : BigDecimal.ZERO;
    }

    public BigDecimal getTotalHeldEscrow() {
        return totalHeldEscrow != null ? totalHeldEscrow : BigDecimal.ZERO;
    }

    public void setTotalHeldEscrow(BigDecimal totalHeldEscrow) {
        this.totalHeldEscrow = totalHeldEscrow != null ? totalHeldEscrow : BigDecimal.ZERO;
    }

    public BigDecimal getTotalDisbursedPayouts() {
        return totalDisbursedPayouts != null ? totalDisbursedPayouts : BigDecimal.ZERO;
    }

    public void setTotalDisbursedPayouts(BigDecimal totalDisbursedPayouts) {
        this.totalDisbursedPayouts = totalDisbursedPayouts != null ? totalDisbursedPayouts : BigDecimal.ZERO;
    }

    public long getTotalSuccessfulPayments() {
        return totalSuccessfulPayments;
    }

    public void setTotalSuccessfulPayments(long totalSuccessfulPayments) {
        this.totalSuccessfulPayments = totalSuccessfulPayments;
    }

    public BigDecimal getTotalSuccessfulPaymentsAmount() {
        return totalSuccessfulPaymentsAmount != null ? totalSuccessfulPaymentsAmount : BigDecimal.ZERO;
    }

    public void setTotalSuccessfulPaymentsAmount(BigDecimal totalSuccessfulPaymentsAmount) {
        this.totalSuccessfulPaymentsAmount = totalSuccessfulPaymentsAmount != null ? totalSuccessfulPaymentsAmount : BigDecimal.ZERO;
    }

    public long getTotalPendingPayments() {
        return totalPendingPayments;
    }

    public void setTotalPendingPayments(long totalPendingPayments) {
        this.totalPendingPayments = totalPendingPayments;
    }

    public BigDecimal getTotalPendingPaymentsAmount() {
        return totalPendingPaymentsAmount != null ? totalPendingPaymentsAmount : BigDecimal.ZERO;
    }

    public void setTotalPendingPaymentsAmount(BigDecimal totalPendingPaymentsAmount) {
        this.totalPendingPaymentsAmount = totalPendingPaymentsAmount != null ? totalPendingPaymentsAmount : BigDecimal.ZERO;
    }

    public BigDecimal getTotalRefunded() {
        return totalRefunded != null ? totalRefunded : BigDecimal.ZERO;
    }

    public void setTotalRefunded(BigDecimal totalRefunded) {
        this.totalRefunded = totalRefunded != null ? totalRefunded : BigDecimal.ZERO;
    }

    public long getTotalRefundsCount() {
        return totalRefundsCount;
    }

    public void setTotalRefundsCount(long totalRefundsCount) {
        this.totalRefundsCount = totalRefundsCount;
    }

    public long getActiveDisputesCount() {
        return activeDisputesCount;
    }

    public void setActiveDisputesCount(long activeDisputesCount) {
        this.activeDisputesCount = activeDisputesCount;
    }

    public long getTotalDisputesCount() {
        return totalDisputesCount;
    }

    public void setTotalDisputesCount(long totalDisputesCount) {
        this.totalDisputesCount = totalDisputesCount;
    }

    public long getPendingPayoutsCount() {
        return pendingPayoutsCount;
    }

    public void setPendingPayoutsCount(long pendingPayoutsCount) {
        this.pendingPayoutsCount = pendingPayoutsCount;
    }

    public long getTotalOrdersCount() {
        return totalOrdersCount;
    }

    public void setTotalOrdersCount(long totalOrdersCount) {
        this.totalOrdersCount = totalOrdersCount;
    }

    public String getSelectedRange() {
        return selectedRange != null ? selectedRange : "ALL";
    }

    public void setSelectedRange(String selectedRange) {
        this.selectedRange = selectedRange;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean hasDateFilter() {
        return !"ALL".equalsIgnoreCase(selectedRange);
    }
}
