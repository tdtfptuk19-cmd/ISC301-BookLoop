package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable snapshot record of platform commission and seller net amount for an order.
 */
@Entity
@Table(name = "commission_records", indexes = {
        @Index(name = "idx_comm_rec_order", columnList = "order_id", unique = true),
        @Index(name = "idx_comm_rec_seller", columnList = "seller_id"),
        @Index(name = "idx_comm_rec_status", columnList = "status")
})
public class CommissionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction_id")
    private PaymentTransaction paymentTransaction;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "gross_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @NotNull
    @DecimalMin("0.0000")
    @Column(name = "commission_rate_snapshot", nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRateSnapshot;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "partner_fee_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal partnerFeeAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "seller_net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellerNetAmount;

    @NotBlank
    @Size(max = 10)
    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "VND";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CommissionStatus status = CommissionStatus.PENDING_SETTLEMENT;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public CommissionRecord() {
    }

    public CommissionRecord(Order order, User seller, PaymentTransaction paymentTransaction,
                            BigDecimal grossAmount, BigDecimal commissionRateSnapshot,
                            BigDecimal commissionAmount, BigDecimal partnerFeeAmount,
                            BigDecimal sellerNetAmount) {
        this.order = order;
        this.seller = seller;
        this.paymentTransaction = paymentTransaction;
        this.grossAmount = grossAmount;
        this.commissionRateSnapshot = commissionRateSnapshot;
        this.commissionAmount = commissionAmount;
        this.partnerFeeAmount = partnerFeeAmount != null ? partnerFeeAmount : BigDecimal.ZERO;
        this.sellerNetAmount = sellerNetAmount;
        this.currency = "VND";
        this.status = CommissionStatus.PENDING_SETTLEMENT;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public PaymentTransaction getPaymentTransaction() {
        return paymentTransaction;
    }

    public void setPaymentTransaction(PaymentTransaction paymentTransaction) {
        this.paymentTransaction = paymentTransaction;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getCommissionRateSnapshot() {
        return commissionRateSnapshot;
    }

    public void setCommissionRateSnapshot(BigDecimal commissionRateSnapshot) {
        this.commissionRateSnapshot = commissionRateSnapshot;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public BigDecimal getPartnerFeeAmount() {
        return partnerFeeAmount;
    }

    public void setPartnerFeeAmount(BigDecimal partnerFeeAmount) {
        this.partnerFeeAmount = partnerFeeAmount;
    }

    public BigDecimal getSellerNetAmount() {
        return sellerNetAmount;
    }

    public void setSellerNetAmount(BigDecimal sellerNetAmount) {
        this.sellerNetAmount = sellerNetAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public CommissionStatus getStatus() {
        return status;
    }

    public void setStatus(CommissionStatus status) {
        this.status = status;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }

    public BigDecimal getOrderGrossAmount() {
        return grossAmount;
    }

    public BigDecimal getGatewayFeeSnapshot() {
        return partnerFeeAmount;
    }

    public BigDecimal getRatePercentageApplied() {
        return commissionRateSnapshot != null ? commissionRateSnapshot.multiply(new BigDecimal("100")) : BigDecimal.ZERO;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

