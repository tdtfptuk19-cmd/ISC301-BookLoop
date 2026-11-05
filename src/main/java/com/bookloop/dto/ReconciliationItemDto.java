package com.bookloop.dto;

import com.bookloop.entity.ReconciliationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detailed audit record for an individual order / transaction comparison.
 */
public class ReconciliationItemDto {

    private Long orderId;
    private String orderNumber;
    private String paymentReference;
    private String providerReference;
    private String paymentMethod;
    private BigDecimal orderGrossAmount;
    private BigDecimal paymentAmount;
    private BigDecimal commissionAmount;
    private BigDecimal payoutAmount;
    private BigDecimal refundAmount;
    private BigDecimal discrepancyAmount;
    private ReconciliationStatus status;
    private String notes;
    private LocalDateTime transactionDate;
    private boolean mockGateway;

    public ReconciliationItemDto() {
        this.orderGrossAmount = BigDecimal.ZERO;
        this.paymentAmount = BigDecimal.ZERO;
        this.commissionAmount = BigDecimal.ZERO;
        this.payoutAmount = BigDecimal.ZERO;
        this.refundAmount = BigDecimal.ZERO;
        this.discrepancyAmount = BigDecimal.ZERO;
        this.mockGateway = true;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getOrderGrossAmount() {
        return orderGrossAmount;
    }

    public void setOrderGrossAmount(BigDecimal orderGrossAmount) {
        this.orderGrossAmount = orderGrossAmount != null ? orderGrossAmount : BigDecimal.ZERO;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount != null ? paymentAmount : BigDecimal.ZERO;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount != null ? commissionAmount : BigDecimal.ZERO;
    }

    public BigDecimal getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(BigDecimal payoutAmount) {
        this.payoutAmount = payoutAmount != null ? payoutAmount : BigDecimal.ZERO;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount != null ? refundAmount : BigDecimal.ZERO;
    }

    public BigDecimal getDiscrepancyAmount() {
        return discrepancyAmount;
    }

    public void setDiscrepancyAmount(BigDecimal discrepancyAmount) {
        this.discrepancyAmount = discrepancyAmount != null ? discrepancyAmount : BigDecimal.ZERO;
    }

    public ReconciliationStatus getStatus() {
        return status;
    }

    public void setStatus(ReconciliationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public boolean isMockGateway() {
        return mockGateway;
    }

    public void setMockGateway(boolean mockGateway) {
        this.mockGateway = mockGateway;
    }
}

