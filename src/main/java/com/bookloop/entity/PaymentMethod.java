package com.bookloop.entity;

/**
 * Supported C2C transaction payment methods in BookLoop.
 *
 * IN_PERSON_MEETUP: Buyer and seller meet physically on campus and pay directly (cash/in-person).
 * BANK_TRANSFER: Buyer transfers money directly to the seller's bank account.
 *
 * BookLoop acts as an intermediary connecting students and does not hold or escrow funds.
 */
public enum PaymentMethod {
    MARKETPLACE_HOLDING("Thanh toán Trực tuyến Qua Đối tác (Tạm giữ có điều kiện - Bảo vệ người mua)"),
    IN_PERSON_MEETUP("Gặp trực tiếp tại Campus"),
    BANK_TRANSFER("Chuyển khoản ngân hàng trực tiếp");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
