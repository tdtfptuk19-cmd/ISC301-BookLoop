package com.bookloop.dto;

import com.bookloop.entity.SellerPaymentAccount;

import java.math.BigDecimal;

public class SellerFinanceOverviewDto {

    private BigDecimal totalRevenue;
    private BigDecimal pendingEscrowAmount;
    private BigDecimal netDisbursed;
    private BigDecimal totalCommissionPaid;
    private long pendingOrdersCount;
    private long deliveredOrdersCount;
    private SellerPaymentAccount paymentAccount;
    private BigDecimal undisbursedRevenue = BigDecimal.ZERO;

    public SellerFinanceOverviewDto() {
        this.totalRevenue = BigDecimal.ZERO;
        this.pendingEscrowAmount = BigDecimal.ZERO;
        this.netDisbursed = BigDecimal.ZERO;
        this.totalCommissionPaid = BigDecimal.ZERO;
        this.undisbursedRevenue = BigDecimal.ZERO;
        this.pendingOrdersCount = 0;
        this.deliveredOrdersCount = 0;
    }

    public BigDecimal getUndisbursedRevenue() {
        return undisbursedRevenue != null ? undisbursedRevenue : BigDecimal.ZERO;
    }

    public boolean hasUndisbursedRevenue() {
        return undisbursedRevenue != null && undisbursedRevenue.compareTo(BigDecimal.ZERO) > 0;
    }

    public void setUndisbursedRevenue(BigDecimal undisbursedRevenue) {
        this.undisbursedRevenue = undisbursedRevenue;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getPendingEscrowAmount() {
        return pendingEscrowAmount;
    }

    public void setPendingEscrowAmount(BigDecimal pendingEscrowAmount) {
        this.pendingEscrowAmount = pendingEscrowAmount;
    }

    public BigDecimal getNetDisbursed() {
        return netDisbursed;
    }

    public void setNetDisbursed(BigDecimal netDisbursed) {
        this.netDisbursed = netDisbursed;
    }

    public BigDecimal getTotalCommissionPaid() {
        return totalCommissionPaid;
    }

    public void setTotalCommissionPaid(BigDecimal totalCommissionPaid) {
        this.totalCommissionPaid = totalCommissionPaid;
    }

    public long getPendingOrdersCount() {
        return pendingOrdersCount;
    }

    public void setPendingOrdersCount(long pendingOrdersCount) {
        this.pendingOrdersCount = pendingOrdersCount;
    }

    public long getDeliveredOrdersCount() {
        return deliveredOrdersCount;
    }

    public void setDeliveredOrdersCount(long deliveredOrdersCount) {
        this.deliveredOrdersCount = deliveredOrdersCount;
    }

    public SellerPaymentAccount getPaymentAccount() {
        return paymentAccount;
    }

    public void setPaymentAccount(SellerPaymentAccount paymentAccount) {
        this.paymentAccount = paymentAccount;
    }
}

