package com.bookloop.payment.dto;

import com.bookloop.entity.RefundStatus;
import java.math.BigDecimal;

/**
 * Result returned by provider when processing a refund request.
 */
public class RefundResponse {
    private boolean success;
    private String providerRefundId;
    private RefundStatus status;
    private BigDecimal refundedAmount;
    private String failureReason;

    public RefundResponse() {}

    public static RefundResponse success(String providerRefundId, RefundStatus status, BigDecimal refundedAmount) {
        RefundResponse res = new RefundResponse();
        res.success = true;
        res.providerRefundId = providerRefundId;
        res.status = status;
        res.refundedAmount = refundedAmount;
        return res;
    }

    public static RefundResponse failure(String failureReason) {
        RefundResponse res = new RefundResponse();
        res.success = false;
        res.status = RefundStatus.FAILED;
        res.failureReason = failureReason;
        return res;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getProviderRefundId() { return providerRefundId; }
    public void setProviderRefundId(String providerRefundId) { this.providerRefundId = providerRefundId; }

    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }

    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}

