package com.bookloop.repository;

import com.bookloop.entity.PaymentWebhookEvent;
import com.bookloop.entity.WebhookProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentWebhookEventRepository extends JpaRepository<PaymentWebhookEvent, Long> {

    Optional<PaymentWebhookEvent> findByProviderCodeAndProviderEventId(String providerCode, String providerEventId);

    boolean existsByProviderCodeAndProviderEventId(String providerCode, String providerEventId);

    Page<PaymentWebhookEvent> findByProcessingStatus(WebhookProcessingStatus status, Pageable pageable);

    long countByProcessingStatus(WebhookProcessingStatus status);
}

