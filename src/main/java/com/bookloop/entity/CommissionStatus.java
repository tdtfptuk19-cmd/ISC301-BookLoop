package com.bookloop.entity;

/**
 * Settlement status of a commission record.
 */
public enum CommissionStatus {
    PENDING_SETTLEMENT("Chờ quyết toán"),
    SETTLED("Đã quyết toán"),
    REVERSED("Đã đảo ngược/hoàn tiền");

    private final String displayName;

    CommissionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

