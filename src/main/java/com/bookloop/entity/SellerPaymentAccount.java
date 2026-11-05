package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Stores registered payment recipient information for student sellers at the payment provider.
 * Sensitive data such as raw account credentials or OTPs are NEVER stored.
 */
@Entity
@Table(name = "seller_payment_accounts", indexes = {
        @Index(name = "idx_seller_pay_account_seller", columnList = "seller_id"),
        @Index(name = "idx_seller_pay_account_provider", columnList = "provider_code"),
        @Index(name = "idx_seller_pay_account_status", columnList = "status")
})
public class SellerPaymentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @NotBlank
    @Size(max = 50)
    @Column(name = "provider_code", nullable = false, length = 50)
    private String providerCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "provider_recipient_id", nullable = false, length = 100)
    private String providerRecipientId;

    @Size(max = 30)
    @Column(name = "bank_code", length = 30)
    private String bankCode;

    @Size(max = 100)
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Size(max = 50)
    @Column(name = "account_number_mask", length = 50)
    private String accountNumberMask;

    @Transient
    private String accountNumber;

    @Size(max = 30)
    @Column(name = "citizen_id", length = 30)
    private String citizenId;

    @Size(max = 100)
    @Column(name = "account_holder_name", length = 100)
    private String accountHolderName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    private KycStatus kycStatus = KycStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SellerAccountStatus status = SellerAccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public SellerPaymentAccount() {
    }

    public SellerPaymentAccount(User seller, String providerCode, String providerRecipientId,
                                String bankCode, String accountNumberMask, String accountHolderName) {
        this.seller = seller;
        this.providerCode = providerCode;
        this.providerRecipientId = providerRecipientId;
        this.bankCode = bankCode;
        this.accountNumberMask = accountNumberMask;
        this.accountHolderName = accountHolderName;
        this.kycStatus = KycStatus.VERIFIED;
        this.status = SellerAccountStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getProviderRecipientId() {
        return providerRecipientId;
    }

    public void setProviderRecipientId(String providerRecipientId) {
        this.providerRecipientId = providerRecipientId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getAccountNumberMask() {
        return accountNumberMask;
    }

    public void setAccountNumberMask(String accountNumberMask) {
        this.accountNumberMask = accountNumberMask;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }

    public SellerAccountStatus getStatus() {
        return status;
    }

    public void setStatus(SellerAccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber != null ? accountNumber : accountNumberMask;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        if (accountNumber != null && accountNumber.length() >= 4) {
            this.accountNumberMask = "****" + accountNumber.substring(accountNumber.length() - 4);
        } else {
            this.accountNumberMask = accountNumber;
        }
    }

    public String getMaskedAccountNumber() {
        return accountNumberMask;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public String getProviderSellerId() {
        return providerRecipientId;
    }

    public void setProviderSellerId(String providerSellerId) {
        this.providerRecipientId = providerSellerId;
    }
}

