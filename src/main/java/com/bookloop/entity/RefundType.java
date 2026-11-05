package com.bookloop.entity;

/**
 * Type of refund.
 */
public enum RefundType {
    FULL("Hoàn toàn phần"),
    PARTIAL("Hoàn một phần");

    private final String displayName;

    RefundType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

