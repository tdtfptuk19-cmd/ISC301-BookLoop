package com.bookloop.dto;

import jakarta.validation.constraints.Size;

/**
 * Form backing object for admin moderation actions on a listing.
 */
public class ListingModerationForm {

    @Size(max = 1000, message = "Moderation note cannot exceed 1000 characters")
    private String moderationNote;

    public ListingModerationForm() {
    }

    public String getModerationNote() { return moderationNote; }
    public void setModerationNote(String moderationNote) { this.moderationNote = moderationNote; }
}

