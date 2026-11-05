package com.bookloop.entity;

/**
 * The physical condition of a used textbook listing.
 */
public enum Condition {
    NEW("Mới 100%"),
    LIKE_NEW("Như mới (98%)"),
    GOOD("Tốt"),
    ACCEPTABLE("Chấp nhận được");

    private final String displayName;

    Condition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
