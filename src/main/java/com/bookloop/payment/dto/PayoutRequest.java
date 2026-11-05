package com.bookloop.payment.dto;

import java.math.BigDecimal;

/**
 * Request DTO sent to payment partner to disburse split funds to a seller.
 */
public class PayoutRequest {
    private String idempotencyKey;
    private Long orderId;
    private String merchantOrderNumber;
    private String providerPaymentId;
    private String providerRecipientId;
    private BigDecimal amount;
    private String currency = "VND";
    private String transferDescription;

    public PayoutRequest() {}

    public PayoutRequest(String idempotencyKey, Long orderId, String merchantOrderNumber,
                         String providerPaymentId, String providerRecipientId,
                         BigDecimal amount, String currency, String transferDescription) {
        this.idempotencyKey = idempotencyKey;
        this.orderId = orderId;
        this.merchantOrderNumber = merchantOrderNumber;
        this.providerPaymentId = providerPaymentId;
        this.providerRecipientId = providerRecipientId;
        this.amount = amount;
        this.currency = currency != null ? currency : "VND";
        this.transferDescription = transferDescription;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getMerchantOrderNumber() { return merchantOrderNumber; }
    public void setMerchantOrderNumber(String merchantOrderNumber) { this.merchantOrderNumber = merchantOrderNumber; }

    public String getProviderPaymentId() { return providerPaymentId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }

    public String getProviderRecipientId() { return providerRecipientId; }
    public void setProviderRecipientId(String providerRecipientId) { this.providerRecipientId = providerRecipientId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getTransferDescription() { return transferDescription; }
    public void setTransferDescription(String transferDescription) { this.transferDescription = transferDescription; }
}

