package com.bookloop.entity;

/**
 * Processing status of an incoming payment webhook event.
 */
public enum WebhookProcessingStatus {
    PENDING("Chờ xử lý"),
    PROCESSED("Đã xử lý"),
    IGNORED("Bỏ qua (Trùng lặp)"),
    FAILED("Xử lý thất bại");

    private final String displayName;

    WebhookProcessingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

