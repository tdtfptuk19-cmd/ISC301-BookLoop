package com.bookloop.payment.dto;

import com.bookloop.entity.RefundType;
import java.math.BigDecimal;

/**
 * Request DTO to issue a full or partial refund back to buyer through provider.
 */
public class RefundRequest {
    private String idempotencyKey;
    private String providerPaymentId;
    private String merchantReference;
    private BigDecimal refundAmount;
    private RefundType refundType = RefundType.FULL;
    private String reason;

    public RefundRequest() {}

    public RefundRequest(String idempotencyKey, String providerPaymentId, String merchantReference,
                         BigDecimal refundAmount, RefundType refundType, String reason) {
        this.idempotencyKey = idempotencyKey;
        this.providerPaymentId = providerPaymentId;
        this.merchantReference = merchantReference;
        this.refundAmount = refundAmount;
        this.refundType = refundType;
        this.reason = reason;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getProviderPaymentId() { return providerPaymentId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }

    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public RefundType getRefundType() { return refundType; }
    public void setRefundType(RefundType refundType) { this.refundType = refundType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

