package com.bookloop.dto;

import com.bookloop.entity.ReportStatus;
import jakarta.validation.constraints.Size;

/**
 * Form backing object for resolving a report.
 */
public class ReportResolutionForm {

    private ReportStatus newStatus;

    @Size(max = 1000, message = "Admin note cannot exceed 1000 characters")
    private String adminNote;

    public ReportResolutionForm() {
    }

    public ReportStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ReportStatus newStatus) { this.newStatus = newStatus; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
}

