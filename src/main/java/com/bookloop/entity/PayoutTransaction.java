package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks partner split settlement payouts disbursed to sellers.
 */
@Entity
@Table(name = "payout_transactions", indexes = {
        @Index(name = "idx_payout_tx_order", columnList = "order_id"),
        @Index(name = "idx_payout_tx_seller", columnList = "seller_id"),
        @Index(name = "idx_payout_tx_status", columnList = "status"),
        @Index(name = "idx_payout_tx_idempotency", columnList = "idempotency_key", unique = true)
})
public class PayoutTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_record_id")
    private CommissionRecord commissionRecord;

    @NotBlank
    @Size(max = 50)
    @Column(name = "provider_code", nullable = false, length = 50)
    private String providerCode;

    @Size(max = 100)
    @Column(name = "provider_payout_id", length = 100)
    private String providerPayoutId;

    @NotBlank
    @Size(max = 64)
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Size(max = 10)
    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "VND";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PayoutStatus status = PayoutStatus.REQUESTED;

    @Size(max = 1000)
    @Column(name = "failure_reason", columnDefinition = "NVARCHAR(1000)")
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
    }

    public PayoutTransaction() {
    }

    public PayoutTransaction(Order order, User seller, CommissionRecord commissionRecord,
                             String providerCode, String idempotencyKey, BigDecimal amount) {
        this.order = order;
        this.seller = seller;
        this.commissionRecord = commissionRecord;
        this.providerCode = providerCode;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.currency = "VND";
        this.status = PayoutStatus.REQUESTED;
        this.requestedAt = LocalDateTime.now();
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

    public CommissionRecord getCommissionRecord() {
        return commissionRecord;
    }

    public void setCommissionRecord(CommissionRecord commissionRecord) {
        this.commissionRecord = commissionRecord;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getProviderPayoutId() {
        return providerPayoutId;
    }

    public void setProviderPayoutId(String providerPayoutId) {
        this.providerPayoutId = providerPayoutId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PayoutStatus getStatus() {
        return status;
    }

    public void setStatus(PayoutStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

