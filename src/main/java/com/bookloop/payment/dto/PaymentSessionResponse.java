package com.bookloop.payment.dto;

import java.math.BigDecimal;

/**
 * Response DTO returned by payment provider after creating a payment session.
 */
public class PaymentSessionResponse {
    private boolean success;
    private String providerPaymentId;
    private String merchantReference;
    private String qrCodeData;
    private String paymentUrl;
    private BigDecimal amount;
    private String currency;
    private String errorMessage;

    public PaymentSessionResponse() {}

    public static PaymentSessionResponse success(String providerPaymentId, String merchantReference,
                                                 String qrCodeData, String paymentUrl,
                                                 BigDecimal amount, String currency) {
        PaymentSessionResponse res = new PaymentSessionResponse();
        res.success = true;
        res.providerPaymentId = providerPaymentId;
        res.merchantReference = merchantReference;
        res.qrCodeData = qrCodeData;
        res.paymentUrl = paymentUrl;
        res.amount = amount;
        res.currency = currency;
        return res;
    }

    public static PaymentSessionResponse failure(String errorMessage) {
        PaymentSessionResponse res = new PaymentSessionResponse();
        res.success = false;
        res.errorMessage = errorMessage;
        return res;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getProviderPaymentId() { return providerPaymentId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }

    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }

    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }

    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}

