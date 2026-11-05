package com.bookloop.entity;

/**
 * Status lifecycle of a book listing promotion payment.
 */
public enum PromotionPaymentStatus {
    PENDING("Chờ thanh toán"),
    PAID("Đã thanh toán (Đã duyệt)"),
    REJECTED("Bị từ chối"),
    CANCELLED("Đã hủy");

    private final String displayName;

    PromotionPaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

