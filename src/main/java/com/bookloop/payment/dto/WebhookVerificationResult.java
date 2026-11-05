package com.bookloop.payment.dto;

import java.math.BigDecimal;

/**
 * Result of webhook authenticity and payload validation.
 */
public class WebhookVerificationResult {
    private boolean valid;
    private String eventType;
    private String providerEventId;
    private String providerPaymentId;
    private String merchantReference;
    private BigDecimal amount;
    private String currency;
    private String failureReason;

    public WebhookVerificationResult() {}

    public static WebhookVerificationResult success(String eventType, String providerEventId,
                                                    String providerPaymentId, String merchantReference,
                                                    BigDecimal amount, String currency) {
        WebhookVerificationResult res = new WebhookVerificationResult();
        res.valid = true;
        res.eventType = eventType;
        res.providerEventId = providerEventId;
        res.providerPaymentId = providerPaymentId;
        res.merchantReference = merchantReference;
        res.amount = amount;
        res.currency = currency;
        return res;
    }

    public static WebhookVerificationResult failure(String reason) {
        WebhookVerificationResult res = new WebhookVerificationResult();
        res.valid = false;
        res.failureReason = reason;
        return res;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getProviderEventId() { return providerEventId; }
    public void setProviderEventId(String providerEventId) { this.providerEventId = providerEventId; }

    public String getProviderPaymentId() { return providerPaymentId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }

    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}

