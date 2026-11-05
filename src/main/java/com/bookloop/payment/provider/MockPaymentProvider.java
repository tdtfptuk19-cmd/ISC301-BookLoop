package com.bookloop.payment.provider;

import com.bookloop.entity.KycStatus;
import com.bookloop.entity.PaymentStatus;
import com.bookloop.entity.PayoutStatus;
import com.bookloop.entity.RefundStatus;
import com.bookloop.payment.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sandbox/Mock Payment Provider simulating partner-managed conditional fund holding and split settlement.
 * Clearly labeled as DEMO/SANDBOX to ensure transparency.
 */
@Component("mockPaymentProvider")
public class MockPaymentProvider implements PaymentProvider {

    public static final String PROVIDER_CODE = "MOCK_ESCROW";
    public static final String PROVIDER_NAME = "BookLoop Sandbox Escrow Partner [DEMO]";

    @Value("${bookloop.payment.webhook-secret:mock_secret_key_bookloop_2024}")
    private String webhookSecret;

    // In-memory simulated provider ledger for sandbox testing
    private final Map<String, SimulatedTransaction> simulatedTransactions = new ConcurrentHashMap<>();
    private final Map<String, SimulatedPayout> simulatedPayouts = new ConcurrentHashMap<>();

    @Override
    public String getProviderCode() {
        return PROVIDER_CODE;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isConditionalFundHoldingSupported() {
        return true;
    }

    @Override
    public PaymentSessionResponse createPaymentSession(PaymentSessionRequest request) {
        String providerPaymentId = "mock_pay_" + UUID.randomUUID().toString().substring(0, 12);
        String qrPayload = "vietqr://pay?pa=0987654321@bookloop&pn=BookLoop%20Escrow&am="
                + request.getAmount().toPlainString()
                + "&tn=" + request.getMerchantReference();

        String paymentUrl = "/buyer/payment/mock-checkout?ref=" + request.getMerchantReference();

        SimulatedTransaction tx = new SimulatedTransaction(
                providerPaymentId, request.getMerchantReference(), request.getAmount(),
                request.getCurrency(), PaymentStatus.PENDING
        );
        simulatedTransactions.put(request.getMerchantReference(), tx);
        simulatedTransactions.put(providerPaymentId, tx);

        return PaymentSessionResponse.success(
                providerPaymentId, request.getMerchantReference(), qrPayload, paymentUrl,
                request.getAmount(), request.getCurrency()
        );
    }

    @Override
    public PaymentStatusResponse queryPaymentStatus(String providerPaymentId) {
        SimulatedTransaction tx = simulatedTransactions.get(providerPaymentId);
        PaymentStatusResponse res = new PaymentStatusResponse();
        if (tx != null) {
            res.setFound(true);
            res.setProviderPaymentId(tx.providerPaymentId);
            res.setMerchantReference(tx.merchantReference);
            res.setStatus(tx.status);
            res.setAmount(tx.amount);
            res.setCurrency(tx.currency);
            res.setPaidAt(tx.paidAt);
            res.setRawStatus(tx.status.name());
        } else {
            res.setFound(false);
        }
        return res;
    }

    @Override
    public WebhookVerificationResult verifyWebhook(String rawPayload, Map<String, String> headers) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return WebhookVerificationResult.failure("Payload is empty");
        }

        String signature = headers != null ? headers.getOrDefault("X-Signature", headers.get("x-signature")) : null;
        if (signature != null && !signature.isBlank()) {
            String calculated = calculateHmac(rawPayload, webhookSecret);
            if (!calculated.equalsIgnoreCase(signature)) {
                return WebhookVerificationResult.failure("Invalid HMAC signature");
            }
        }

        // Parse minimal mock JSON keys without heavyweight dependencies
        try {
            String eventType = extractJsonValue(rawPayload, "eventType");
            String providerEventId = extractJsonValue(rawPayload, "eventId");
            String providerPaymentId = extractJsonValue(rawPayload, "providerPaymentId");
            String merchantReference = extractJsonValue(rawPayload, "merchantReference");
            String amountStr = extractJsonValue(rawPayload, "amount");
            BigDecimal amount = amountStr != null ? new BigDecimal(amountStr) : BigDecimal.ZERO;
            String currency = extractJsonValue(rawPayload, "currency");

            if (providerEventId == null) {
                providerEventId = "evt_" + UUID.randomUUID().toString().substring(0, 8);
            }

            return WebhookVerificationResult.success(
                    eventType != null ? eventType : "payment.paid_held",
                    providerEventId,
                    providerPaymentId,
                    merchantReference,
                    amount,
                    currency != null ? currency : "VND"
            );
        } catch (Exception ex) {
            return WebhookVerificationResult.failure("Malformed webhook JSON payload: " + ex.getMessage());
        }
    }

    @Override
    public SellerOnboardingResponse registerSellerRecipient(SellerOnboardingRequest request) {
        String recipientId = "mock_rcp_sel_" + request.getSellerId() + "_" + UUID.randomUUID().toString().substring(0, 6);
        String mask = request.getAccountNumber() != null && request.getAccountNumber().length() >= 4
                ? "****" + request.getAccountNumber().substring(request.getAccountNumber().length() - 4)
                : "****8888";

        return SellerOnboardingResponse.success(recipientId, KycStatus.VERIFIED, mask);
    }

    @Override
    public PayoutResponse requestConditionalPayout(PayoutRequest request) {
        String providerPayoutId = "mock_pout_" + UUID.randomUUID().toString().substring(0, 12);
        SimulatedPayout payout = new SimulatedPayout(
                providerPayoutId, request.getIdempotencyKey(), request.getAmount(),
                request.getCurrency(), PayoutStatus.COMPLETED
        );
        simulatedPayouts.put(request.getIdempotencyKey(), payout);
        simulatedPayouts.put(providerPayoutId, payout);

        return PayoutResponse.success(providerPayoutId, PayoutStatus.COMPLETED, request.getAmount());
    }

    @Override
    public PayoutStatusResponse queryPayoutStatus(String providerPayoutId) {
        SimulatedPayout payout = simulatedPayouts.get(providerPayoutId);
        PayoutStatusResponse res = new PayoutStatusResponse();
        if (payout != null) {
            res.setFound(true);
            res.setProviderPayoutId(payout.providerPayoutId);
            res.setStatus(payout.status);
            res.setAmount(payout.amount);
            res.setCompletedAt(payout.completedAt);
        } else {
            res.setFound(false);
        }
        return res;
    }

    @Override
    public RefundResponse requestRefund(RefundRequest request) {
        String refundId = "mock_ref_" + UUID.randomUUID().toString().substring(0, 12);
        return RefundResponse.success(refundId, RefundStatus.COMPLETED, request.getRefundAmount());
    }

    /**
     * Helper for sandbox simulation: marks a transaction as PAID_HELD.
     */
    public void markTransactionPaid(String merchantReference) {
        SimulatedTransaction tx = simulatedTransactions.get(merchantReference);
        if (tx != null) {
            tx.status = PaymentStatus.PAID_HELD;
            tx.paidAt = LocalDateTime.now();
        }
    }

    public String generateValidSignature(String payload) {
        return calculateHmac(payload, webhookSecret);
    }

    private String calculateHmac(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon == -1) return null;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length()) return null;

        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return end != -1 ? json.substring(start + 1, end) : null;
        } else {
            int end = start;
            while (end < json.length() && (Character.isLetterOrDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
                end++;
            }
            return json.substring(start, end);
        }
    }

    private static class SimulatedTransaction {
        String providerPaymentId;
        String merchantReference;
        BigDecimal amount;
        String currency;
        PaymentStatus status;
        LocalDateTime paidAt;

        SimulatedTransaction(String providerPaymentId, String merchantReference, BigDecimal amount,
                             String currency, PaymentStatus status) {
            this.providerPaymentId = providerPaymentId;
            this.merchantReference = merchantReference;
            this.amount = amount;
            this.currency = currency;
            this.status = status;
        }
    }

    private static class SimulatedPayout {
        String providerPayoutId;
        String idempotencyKey;
        BigDecimal amount;
        String currency;
        PayoutStatus status;
        LocalDateTime completedAt;

        SimulatedPayout(String providerPayoutId, String idempotencyKey, BigDecimal amount,
                        String currency, PayoutStatus status) {
            this.providerPayoutId = providerPayoutId;
            this.idempotencyKey = idempotencyKey;
            this.amount = amount;
            this.currency = currency;
            this.status = status;
            this.completedAt = LocalDateTime.now();
        }
    }
}

