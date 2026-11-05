package com.bookloop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form backing object for reporting a listing.
 */
public class ReportForm {

    private Long productId;
    private Long reportedUserId;

    @NotBlank(message = "Please provide a reason for the report")
    @Size(max = 200, message = "Reason cannot exceed 200 characters")
    private String reason;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    public ReportForm() {
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

