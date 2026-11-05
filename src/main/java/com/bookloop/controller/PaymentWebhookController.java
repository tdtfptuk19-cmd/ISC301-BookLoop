package com.bookloop.controller;

import com.bookloop.service.PaymentManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller receiving incoming webhooks from payment providers.
 * Idempotent, validates HMAC signatures, and updates holding transactions.
 */
@RestController
@RequestMapping("/api/webhooks/payments")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final PaymentManagerService paymentManagerService;

    public PaymentWebhookController(PaymentManagerService paymentManagerService) {
        this.paymentManagerService = paymentManagerService;
    }

    @PostMapping("/{provider}")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @PathVariable("provider") String provider,
            @RequestBody String payload,
            @RequestHeader(value = "X-BookLoop-Signature", required = false) String bookloopSig,
            @RequestHeader(value = "X-Signature", required = false) String standardSig,
            @RequestHeader(value = "X-Timestamp", required = false) String timestampHeader) {

        String signature = bookloopSig != null ? bookloopSig : standardSig;
        log.info("Received payment webhook for provider '{}' with signature present: {}", provider, signature != null);

        Map<String, Object> response = new HashMap<>();

        Map<String, String> headerMap = new HashMap<>();
        if (signature != null) {
            headerMap.put("X-Signature", signature);
            headerMap.put("X-BookLoop-Signature", signature);
        }
        if (timestampHeader != null) {
            headerMap.put("X-Timestamp", timestampHeader);
        }

        try {
            boolean success = paymentManagerService.processWebhook(provider, payload, headerMap);
            if (success) {
                response.put("status", "SUCCESS");
                response.put("message", "Webhook processed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "IGNORED");
                response.put("message", "Webhook ignored or duplicate");
                return ResponseEntity.ok(response);
            }
        } catch (SecurityException ex) {
            log.warn("Invalid webhook signature for provider '{}': {}", provider, ex.getMessage());
            response.put("status", "REJECTED");
            response.put("error", "Invalid webhook signature");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception ex) {
            log.error("Error processing payment webhook: {}", ex.getMessage(), ex);
            response.put("status", "ERROR");
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
