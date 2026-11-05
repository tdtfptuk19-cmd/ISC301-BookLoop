package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks promotional / listing fees paid by sellers to BookLoop.
 * Kept strictly isolated from buyer-seller book orders.
 */
@Entity
@Table(name = "promotion_payments", indexes = {
        @Index(name = "idx_prom_seller", columnList = "seller_id"),
        @Index(name = "idx_prom_product", columnList = "product_id"),
        @Index(name = "idx_prom_status", columnList = "status"),
        @Index(name = "idx_prom_ref", columnList = "transaction_reference")
})
public class PromotionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "fee_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "duration_days", nullable = false)
    private int durationDays = 7;

    @Size(max = 50)
    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "BANK_TRANSFER";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PromotionPaymentStatus status = PromotionPaymentStatus.PENDING;

    @Size(max = 100)
    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Size(max = 1000)
    @Column(name = "payment_notes", columnDefinition = "NVARCHAR(1000)")
    private String paymentNotes;

    @Size(max = 1000)
    @Column(name = "admin_note", columnDefinition = "NVARCHAR(1000)")
    private String adminNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private User verifiedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    public PromotionPayment() {}

    public PromotionPayment(User seller, Product product, BigDecimal feeAmount, int durationDays, String transactionReference) {
        this.seller = seller;
        this.product = product;
        this.feeAmount = feeAmount;
        this.durationDays = durationDays;
        this.transactionReference = transactionReference;
        this.status = PromotionPaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public PromotionPaymentStatus getStatus() { return status; }
    public void setStatus(PromotionPaymentStatus status) { this.status = status; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getPaymentNotes() { return paymentNotes; }
    public void setPaymentNotes(String paymentNotes) { this.paymentNotes = paymentNotes; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public User getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(User verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
}

