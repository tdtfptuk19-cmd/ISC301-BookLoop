package com.bookloop.entity;

/**
 * Status of a seller payment account.
 */
public enum SellerAccountStatus {
    ACTIVE("Đang hoạt động"),
    INACTIVE("Tạm ngừng");

    private final String displayName;

    SellerAccountStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

