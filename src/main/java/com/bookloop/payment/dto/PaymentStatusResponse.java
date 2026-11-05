package com.bookloop.payment.dto;

import com.bookloop.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Result of querying payment transaction status from provider.
 */
public class PaymentStatusResponse {
    private boolean found;
    private String providerPaymentId;
    private String merchantReference;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime paidAt;
    private String rawStatus;

    public PaymentStatusResponse() {}

    public boolean isFound() { return found; }
    public void setFound(boolean found) { this.found = found; }

    public String getProviderPaymentId() { return providerPaymentId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }

    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getRawStatus() { return rawStatus; }
    public void setRawStatus(String rawStatus) { this.rawStatus = rawStatus; }
}

