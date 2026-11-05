package com.bookloop.payment.dto;

import com.bookloop.entity.KycStatus;

/**
 * Result of seller recipient onboarding at payment provider.
 */
public class SellerOnboardingResponse {
    private boolean success;
    private String providerRecipientId;
    private KycStatus kycStatus;
    private String accountNumberMask;
    private String errorMessage;

    public SellerOnboardingResponse() {}

    public static SellerOnboardingResponse success(String providerRecipientId, KycStatus kycStatus, String accountNumberMask) {
        SellerOnboardingResponse res = new SellerOnboardingResponse();
        res.success = true;
        res.providerRecipientId = providerRecipientId;
        res.kycStatus = kycStatus;
        res.accountNumberMask = accountNumberMask;
        return res;
    }

    public static SellerOnboardingResponse failure(String errorMessage) {
        SellerOnboardingResponse res = new SellerOnboardingResponse();
        res.success = false;
        res.errorMessage = errorMessage;
        return res;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getProviderRecipientId() { return providerRecipientId; }
    public void setProviderRecipientId(String providerRecipientId) { this.providerRecipientId = providerRecipientId; }

    public String getProviderSellerId() { return providerRecipientId; }
    public void setProviderSellerId(String providerSellerId) { this.providerRecipientId = providerSellerId; }

    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }

    public String getAccountNumberMask() { return accountNumberMask; }
    public void setAccountNumberMask(String accountNumberMask) { this.accountNumberMask = accountNumberMask; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}

