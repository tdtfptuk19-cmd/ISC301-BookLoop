package com.bookloop.entity;

/**
 * Status of a seller disbursement/payout transaction.
 */
public enum PayoutStatus {
    PENDING("Chờ giải ngân", "badge-pending"),
    REQUESTED("Đã yêu cầu giải ngân", "badge-pending"),
    PROCESSING("Đang xử lý giải ngân", "badge-pending"),
    PAID("Giải ngân thành công", "badge-confirmed"),
    CONFIRMED("Đã xác nhận giải ngân", "badge-confirmed"),
    COMPLETED("Giải ngân thành công", "badge-confirmed"),
    FAILED("Giải ngân thất bại", "badge-cancelled"),
    CANCELLED("Đã hủy", "badge-cancelled");

    private final String displayName;
    private final String badgeClass;

    PayoutStatus(String displayName, String badgeClass) {
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

