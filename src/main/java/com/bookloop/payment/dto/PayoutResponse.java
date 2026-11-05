package com.bookloop.payment.dto;

import com.bookloop.entity.PayoutStatus;
import java.math.BigDecimal;

/**
 * Result returned by payment provider when initiating a payout disbursement.
 */
public class PayoutResponse {
    private boolean success;
    private String providerPayoutId;
    private PayoutStatus status;
    private BigDecimal amount;
    private String failureReason;

    public PayoutResponse() {}

    public static PayoutResponse success(String providerPayoutId, PayoutStatus status, BigDecimal amount) {
        PayoutResponse res = new PayoutResponse();
        res.success = true;
        res.providerPayoutId = providerPayoutId;
        res.status = status;
        res.amount = amount;
        return res;
    }

    public static PayoutResponse failure(String failureReason) {
        PayoutResponse res = new PayoutResponse();
        res.success = false;
        res.status = PayoutStatus.FAILED;
        res.failureReason = failureReason;
        return res;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getProviderPayoutId() { return providerPayoutId; }
    public void setProviderPayoutId(String providerPayoutId) { this.providerPayoutId = providerPayoutId; }

    public PayoutStatus getStatus() { return status; }
    public void setStatus(PayoutStatus status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}

