package com.bookloop.entity;

/**
 * Represents the lifecycle status of an order in BookLoop.
 *
 * Workflow:
 * PENDING -> CONFIRMED -> COMPLETED
 * PENDING -> CANCELLED
 * CONFIRMED -> CANCELLED
 *
 * COMPLETED orders are final and cannot be cancelled.
 */
public enum OrderStatus {
    PENDING("Chờ người bán xác nhận", "badge-pending"),
    AWAITING_PAYMENT("Chờ người mua thanh toán", "badge-pending"),
    PAID_HELD("Đối tác đang giữ tiền", "badge-confirmed"),
    CONFIRMED("Người bán đã xác nhận", "badge-confirmed"),
    SHIPPED("Người bán đang giao sách", "badge-confirmed"),
    DELIVERED("Đã nhận sách (Chờ giải ngân)", "badge-confirmed"),
    DISPUTED("Đang khiếu nại", "badge-cancelled"),
    REFUNDED("Đã hoàn tiền", "badge-cancelled"),
    COMPLETED("Hoàn thành", "badge-completed"),
    CANCELLED("Đã hủy", "badge-cancelled");

    private final String displayName;
    private final String badgeClass;

    OrderStatus(String displayName, String badgeClass) {
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
