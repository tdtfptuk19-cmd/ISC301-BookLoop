package com.bookloop.payment.dto;

import com.bookloop.entity.PayoutStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Result of querying payout status from provider.
 */
public class PayoutStatusResponse {
    private boolean found;
    private String providerPayoutId;
    private PayoutStatus status;
    private BigDecimal amount;
    private LocalDateTime completedAt;
    private String failureReason;

    public PayoutStatusResponse() {}

    public boolean isFound() { return found; }
    public void setFound(boolean found) { this.found = found; }

    public String getProviderPayoutId() { return providerPayoutId; }
    public void setProviderPayoutId(String providerPayoutId) { this.providerPayoutId = providerPayoutId; }

    public PayoutStatus getStatus() { return status; }
    public void setStatus(PayoutStatus status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}

