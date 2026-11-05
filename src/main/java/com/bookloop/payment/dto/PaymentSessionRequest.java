package com.bookloop.payment.dto;

import java.math.BigDecimal;

/**
 * Request DTO to create a conditional holding payment session.
 */
public class PaymentSessionRequest {
    private String merchantReference;
    private Long orderId;
    private BigDecimal amount;
    private String currency = "VND";
    private String orderDescription;
    private String buyerEmail;
    private String buyerName;
    private String returnUrl;

    public PaymentSessionRequest() {}

    public PaymentSessionRequest(String merchantReference, Long orderId, BigDecimal amount,
                                 String currency, String orderDescription, String buyerEmail,
                                 String buyerName, String returnUrl) {
        this.merchantReference = merchantReference;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency != null ? currency : "VND";
        this.orderDescription = orderDescription;
        this.buyerEmail = buyerEmail;
        this.buyerName = buyerName;
        this.returnUrl = returnUrl;
    }

    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getOrderDescription() { return orderDescription; }
    public void setOrderDescription(String orderDescription) { this.orderDescription = orderDescription; }

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
}

