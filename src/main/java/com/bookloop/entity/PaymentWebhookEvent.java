package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Stores raw incoming webhook events from payment partners for audit and idempotent processing.
 */
@Entity
@Table(name = "payment_webhook_events", indexes = {
        @Index(name = "idx_webhook_prov_event", columnList = "provider_code, provider_event_id"),
        @Index(name = "idx_webhook_status", columnList = "processing_status")
})
public class PaymentWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "provider_code", nullable = false, length = 50)
    private String providerCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "provider_event_id", nullable = false, length = 100)
    private String providerEventId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "signature_verified", nullable = false)
    private boolean signatureVerified = false;

    @NotBlank
    @Column(name = "raw_payload", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String rawPayload;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 30)
    private WebhookProcessingStatus processingStatus = WebhookProcessingStatus.PENDING;

    @Size(max = 2000)
    @Column(name = "error_message", columnDefinition = "NVARCHAR(2000)")
    private String errorMessage;

    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }

    public PaymentWebhookEvent() {
    }

    public PaymentWebhookEvent(String providerCode, String providerEventId, String eventType,
                               boolean signatureVerified, String rawPayload) {
        this.providerCode = providerCode;
        this.providerEventId = providerEventId;
        this.eventType = eventType;
        this.signatureVerified = signatureVerified;
        this.rawPayload = rawPayload;
        this.processingStatus = WebhookProcessingStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getProviderEventId() {
        return providerEventId;
    }

    public void setProviderEventId(String providerEventId) {
        this.providerEventId = providerEventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public boolean isSignatureVerified() {
        return signatureVerified;
    }

    public void setSignatureVerified(boolean signatureVerified) {
        this.signatureVerified = signatureVerified;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public WebhookProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(WebhookProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}

