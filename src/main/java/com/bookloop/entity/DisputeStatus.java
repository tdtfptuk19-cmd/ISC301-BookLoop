package com.bookloop.entity;

/**
 * Status of an order dispute.
 */
public enum DisputeStatus {
    OPEN("Đang khiếu nại", "badge-pending"),
    UNDER_REVIEW("Admin đang xem xét", "badge-pending"),
    RESOLVED_REFUND("Chấp nhận hoàn tiền", "badge-confirmed"),
    RESOLVED_RELEASE("Bác khiếu nại - Giải ngân", "badge-confirmed"),
    CANCELLED("Người mua đã rút khiếu nại", "badge-cancelled");

    private final String displayName;
    private final String badgeClass;

    DisputeStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}

