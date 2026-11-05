package com.bookloop.service;

import com.bookloop.entity.*;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.repository.AdminLogRepository;
import com.bookloop.repository.ProductRepository;
import com.bookloop.repository.PromotionPaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service managing book listing promotional fee workflow and admin verification.
 */
@Service
@Transactional
public class PromotionService {

    private final PromotionPaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final PlatformSettingService settingService;
    private final AdminLogRepository adminLogRepository;

    public PromotionService(PromotionPaymentRepository paymentRepository,
                            ProductRepository productRepository,
                            PlatformSettingService settingService,
                            AdminLogRepository adminLogRepository) {
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.settingService = settingService;
        this.adminLogRepository = adminLogRepository;
    }

    /**
     * Creates a fee payment record for a newly submitted listing.
     * Idempotent: if a PENDING payment already exists for this listing, returns it.
     */
    public PromotionPayment createPaymentForNewListing(Product product, User seller) {
        // Prevent duplicate payment creation for same listing
        return paymentRepository.findTopByProductIdOrderByCreatedAtDesc(product.getId())
                .filter(p -> p.getStatus() == PromotionPaymentStatus.PENDING)
                .orElseGet(() -> {
                    BigDecimal fee = settingService.getListingPromotionFee();
                    int duration = settingService.getPromotionDurationDays();
                    String ref = "BL-PRM-" + product.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                    PromotionPayment payment = new PromotionPayment(seller, product, fee, duration, ref);
                    return paymentRepository.save(payment);
                });
    }

    @Transactional(readOnly = true)
    public PromotionPayment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch thanh toán: " + id));
    }

    @Transactional(readOnly = true)
    public PromotionPayment getPaymentForListing(Long productId) {
        return paymentRepository.findTopByProductIdOrderByCreatedAtDesc(productId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PromotionPayment> getSellerPayments(User seller) {
        return paymentRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId());
    }

    @Transactional(readOnly = true)
    public Page<PromotionPayment> getPaymentsForAdmin(PromotionPaymentStatus status, Pageable pageable) {
        return paymentRepository.findAllWithFilters(status, pageable);
    }

    /**
     * Seller submits their bank transfer reference code / payment proof note.
     */
    public PromotionPayment submitPaymentProof(Long paymentId, String reference, String notes, User seller) {
        PromotionPayment payment = getPaymentById(paymentId);
        if (!payment.getSeller().getId().equals(seller.getId())) {
            throw new UnauthorizedOperationException("Bạn không có quyền cập nhật giao dịch này.");
        }
        if (payment.getStatus() != PromotionPaymentStatus.PENDING) {
            throw new IllegalStateException("Giao dịch này không ở trạng thái chờ thanh toán.");
        }

        if (reference != null && !reference.isBlank()) {
            payment.setTransactionReference(reference.trim());
        }
        payment.setPaymentNotes(notes);
        return paymentRepository.save(payment);
    }

    /**
     * Admin approves or rejects a promotion payment.
     */
    public PromotionPayment verifyPayment(Long paymentId, boolean approved, String adminNote, User admin) {
        PromotionPayment payment = getPaymentById(paymentId);
        Product product = payment.getProduct();

        if (approved) {
            payment.setStatus(PromotionPaymentStatus.PAID);
            payment.setVerifiedAt(LocalDateTime.now());
            payment.setVerifiedBy(admin);
            payment.setAdminNote(adminNote);

            // Promote listing
            product.setPromoted(true);
            product.setPromotedUntil(LocalDateTime.now().plusDays(payment.getDurationDays()));
            productRepository.save(product);

            adminLogRepository.save(new AdminLog(admin, "PROMOTION_PAYMENT_APPROVED", "PROMOTION_PAYMENT",
                    payment.getId(), String.format("Duyệt thanh toán phí quảng cáo #%s cho sách '%s'",
                    payment.getTransactionReference(), product.getTitle())));
        } else {
            payment.setStatus(PromotionPaymentStatus.REJECTED);
            payment.setVerifiedAt(LocalDateTime.now());
            payment.setVerifiedBy(admin);
            payment.setAdminNote(adminNote != null ? adminNote : "Thanh toán không hợp lệ hoặc chưa nhận được tiền");

            adminLogRepository.save(new AdminLog(admin, "PROMOTION_PAYMENT_REJECTED", "PROMOTION_PAYMENT",
                    payment.getId(), String.format("Từ chối thanh toán phí quảng cáo #%s. Lý do: %s",
                    payment.getTransactionReference(), adminNote)));
        }

        return paymentRepository.save(payment);
    }
}

