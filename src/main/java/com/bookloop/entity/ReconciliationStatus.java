package com.bookloop.entity;

/**
 * Status of financial reconciliation between internal ledger, orders, and payment gateway.
 */
public enum ReconciliationStatus {
    MATCHED("Khớp hoàn toàn", "badge bg-success"),
    MISMATCH("Sai lệch số dư / công thức", "badge bg-danger"),
    MISSING_PROVIDER_DATA("Thiếu dữ liệu cổng thanh toán", "badge bg-warning text-dark"),
    MISSING_INTERNAL_DATA("Thiếu dữ liệu nội bộ sàn", "badge bg-secondary"),
    PENDING("Chờ xử lý / Đang giữ Escrow", "badge bg-info text-dark"),
    ERROR("Lỗi kiểm toán", "badge bg-danger");

    private final String displayName;
    private final String badgeClass;

    ReconciliationStatus(String displayName, String badgeClass) {
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

