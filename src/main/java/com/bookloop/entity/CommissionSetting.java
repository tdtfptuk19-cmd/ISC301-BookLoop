package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Admin-configurable marketplace platform commission rates and effective dates.
 */
@Entity
@Table(name = "commission_settings")
public class CommissionSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin("0.0000")
    @DecimalMax("1.0000")
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate; // e.g., 0.0500 for 5.00%

    @NotNull
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Size(max = 500)
    @Column(name = "notes", columnDefinition = "NVARCHAR(500)")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public CommissionSetting() {
    }

    public CommissionSetting(BigDecimal commissionRate, LocalDateTime effectiveFrom, String notes, User updatedBy) {
        this.commissionRate = commissionRate;
        this.effectiveFrom = effectiveFrom;
        this.notes = notes;
        this.updatedBy = updatedBy;
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigDecimal getRatePercentage() {
        return commissionRate != null ? commissionRate.multiply(new BigDecimal("100")) : BigDecimal.ZERO;
    }

    public void setRatePercentage(BigDecimal ratePercentage) {
        this.commissionRate = ratePercentage != null ? ratePercentage.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getFixedFee() {
        return BigDecimal.ZERO;
    }

    public void setFixedFee(BigDecimal fixedFee) {
        // Reserved for future fixed fee support
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

