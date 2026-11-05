package com.bookloop.dto;

import com.bookloop.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Form object capturing buyer checkout information.
 */
public class CheckoutForm {

    @NotNull(message = "Vui lòng chọn phương thức giao dịch")
    private PaymentMethod paymentMethod = PaymentMethod.MARKETPLACE_HOLDING;

    @Size(max = 1000, message = "Ghi chú điểm hẹn không được vượt quá 1000 ký tự")
    private String meetupNote;

    @Size(max = 20, message = "Số điện thoại liên hệ không được vượt quá 20 ký tự")
    private String contactPhone;

    public CheckoutForm() {
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getMeetupNote() {
        return meetupNote;
    }

    public void setMeetupNote(String meetupNote) {
        this.meetupNote = meetupNote;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
