package com.bookloop.entity;

/**
 * Academic classification of a textbook for a given course.
 */
public enum CourseBookRelationshipType {
    REQUIRED("Giáo trình bắt buộc chính thức"),
    RECOMMENDED("Giáo trình khuyến nghị chính thức"),
    SUGGESTED_REFERENCE("Giáo trình tham khảo đề xuất");

    private final String displayName;

    CourseBookRelationshipType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

