package com.bookloop.entity;

/**
 * Status of a payment transaction in the marketplace fund-holding lifecycle.
 */
public enum PaymentStatus {
    INITIATED("Khởi tạo phiên", "badge-pending"),
    PENDING("Chờ thanh toán", "badge-pending"),
    PAID_HELD("Đối tác đang giữ tiền (Escrow)", "badge-confirmed"),
    HELD("Đối tác đang giữ tiền", "badge-confirmed"),
    SETTLED("Đã quyết toán giải ngân", "badge-confirmed"),
    FAILED("Thanh toán thất bại", "badge-cancelled"),
    CANCELLED("Đã hủy", "badge-cancelled"),
    REFUNDED("Đã hoàn tiền", "badge-cancelled"),
    PARTIALLY_REFUNDED("Hoàn tiền một phần", "badge-pending");

    private final String displayName;
    private final String badgeClass;

    PaymentStatus(String displayName, String badgeClass) {
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

