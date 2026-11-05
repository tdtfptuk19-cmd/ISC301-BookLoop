package com.bookloop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Form object capturing review submission from a buyer for a completed order.
 */
public class ReviewForm {

    @NotNull(message = "Order reference is required")
    private Long orderId;

    @NotNull(message = "Seller reference is required")
    private Long sellerId;

    @NotNull(message = "Please select a star rating")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    private Integer rating = 5;

    @Size(max = 2000, message = "Review comment cannot exceed 2000 characters")
    private String comment;

    public ReviewForm() {
    }

    public ReviewForm(Long orderId, Long sellerId) {
        this.orderId = orderId;
        this.sellerId = sellerId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
