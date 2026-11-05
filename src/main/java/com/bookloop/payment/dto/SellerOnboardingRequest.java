package com.bookloop.payment.dto;

import com.bookloop.entity.KycStatus;

/**
 * DTO for onboarding/linking a seller recipient account at the payment provider.
 */
public class SellerOnboardingRequest {
    private Long sellerId;
    private String sellerEmail;
    private String sellerFullName;
    private String sellerPhone;
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private String citizenId;

    public SellerOnboardingRequest() {}

    public SellerOnboardingRequest(Long sellerId, String sellerEmail, String sellerFullName,
                                   String sellerPhone, String bankCode, String accountNumber,
                                   String accountHolderName) {
        this.sellerId = sellerId;
        this.sellerEmail = sellerEmail;
        this.sellerFullName = sellerFullName;
        this.sellerPhone = sellerPhone;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
    }

    public SellerOnboardingRequest(Long sellerId, String sellerFullName, String sellerEmail,
                                   String bankCode, String bankName, String accountNumber,
                                   String accountHolderName, String citizenId) {
        this.sellerId = sellerId;
        this.sellerFullName = sellerFullName;
        this.sellerEmail = sellerEmail;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.citizenId = citizenId;
    }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

    public String getEmail() { return sellerEmail; }
    public void setEmail(String email) { this.sellerEmail = email; }

    public String getSellerFullName() { return sellerFullName; }
    public void setSellerFullName(String sellerFullName) { this.sellerFullName = sellerFullName; }

    public String getFullName() { return sellerFullName; }
    public void setFullName(String fullName) { this.sellerFullName = fullName; }

    public String getSellerPhone() { return sellerPhone; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }
}
