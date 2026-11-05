package com.bookloop.entity;

/**
 * The listing status of a Product.
 */
public enum ListingStatus {
    AVAILABLE("Sẵn sàng giao dịch"),
    SOLD("Sản phẩm đã hết hàng"),
    HIDDEN("Sách chưa được duyệt");

    private final String displayName;

    ListingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
