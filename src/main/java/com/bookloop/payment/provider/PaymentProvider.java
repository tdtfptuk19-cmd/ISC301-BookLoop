package com.bookloop.payment.provider;

import com.bookloop.payment.dto.*;
import java.util.Map;

/**
 * Standard abstraction interface for BookLoop marketplace payment integrations.
 * Supports conditional fund holding (escrow), seller onboarding/KYC, split disbursements, and refunds.
 */
public interface PaymentProvider {

    /**
     * Unique code identifying the provider (e.g. MOCK_ESCROW, VNPAY_MARKETPLACE).
     */
    String getProviderCode();

    /**
     * Human-readable provider display name.
     */
    String getProviderName();

    /**
     * Whether this provider contractually and technically supports partner-managed
     * conditional fund holding and split settlement.
     */
    boolean isConditionalFundHoldingSupported();

    /**
     * Creates a payment session with QR code or redirect URL.
     */
    PaymentSessionResponse createPaymentSession(PaymentSessionRequest request);

    /**
     * Queries real-time payment status from provider API.
     */
    PaymentStatusResponse queryPaymentStatus(String providerPaymentId);

    /**
     * Validates incoming webhook cryptographic signature, timestamp tolerance, and payload structure.
     */
    WebhookVerificationResult verifyWebhook(String rawPayload, Map<String, String> headers);

    /**
     * Registers or verifies a student seller's payout recipient identity at the partner.
     */
    SellerOnboardingResponse registerSellerRecipient(SellerOnboardingRequest request);

    default SellerOnboardingResponse onboardSeller(SellerOnboardingRequest request) {
        return registerSellerRecipient(request);
    }

    /**
     * Requests conditional payout disbursement once buyer confirmed receipt.
     */
    PayoutResponse requestConditionalPayout(PayoutRequest request);

    /**
     * Queries status of an existing payout request.
     */
    PayoutStatusResponse queryPayoutStatus(String providerPayoutId);

    /**
     * Requests full or partial refund back to buyer.
     */
    RefundResponse requestRefund(RefundRequest request);
}

