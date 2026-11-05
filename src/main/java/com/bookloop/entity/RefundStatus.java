package com.bookloop.entity;

/**
 * Status of a refund transaction.
 */
public enum RefundStatus {
    REQUESTED("Đã yêu cầu hoàn tiền", "badge-pending"),
    PROCESSING("Đang xử lý hoàn tiền", "badge-pending"),
    REFUNDED("Hoàn tiền thành công", "badge-confirmed"),
    CONFIRMED("Đã xác nhận hoàn tiền", "badge-confirmed"),
    COMPLETED("Hoàn tiền thành công", "badge-confirmed"),
    FAILED("Hoàn tiền thất bại", "badge-cancelled"),
    REJECTED("Yêu cầu bị từ chối", "badge-cancelled");

    private final String displayName;
    private final String badgeClass;

    RefundStatus(String displayName, String badgeClass) {
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

