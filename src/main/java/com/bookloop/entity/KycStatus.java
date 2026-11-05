package com.bookloop.entity;

/**
 * KYC verification status for seller payment recipient accounts.
 */
public enum KycStatus {
    PENDING("Chờ xác minh", "badge-pending"),
    VERIFIED("Đã xác minh KYC", "badge-confirmed"),
    REJECTED("Bị từ chối", "badge-cancelled");

    private final String displayName;
    private final String badgeClass;

    KycStatus(String displayName, String badgeClass) {
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

