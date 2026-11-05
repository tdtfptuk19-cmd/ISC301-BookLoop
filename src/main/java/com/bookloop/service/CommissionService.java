package com.bookloop.service;

import com.bookloop.entity.*;
import com.bookloop.repository.CommissionRecordRepository;
import com.bookloop.repository.CommissionSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Service managing dynamic platform commission settings, rate snapshots, and settlements.
 */
@Service
@Transactional
public class CommissionService {

    public static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.0500"); // 5% default

    private final CommissionSettingRepository settingRepository;
    private final CommissionRecordRepository recordRepository;
    private final FinancialAuditService auditService;

    public CommissionService(CommissionSettingRepository settingRepository,
                             CommissionRecordRepository recordRepository,
                             FinancialAuditService auditService) {
        this.settingRepository = settingRepository;
        this.recordRepository = recordRepository;
        this.auditService = auditService;
    }

    /**
     * Retrieves the currently active commission rate snapshot, or default (5.00%).
     */
    @Transactional(readOnly = true)
    public BigDecimal getActiveCommissionRate() {
        return settingRepository.findActiveSetting(LocalDateTime.now())
                .map(CommissionSetting::getCommissionRate)
                .orElse(DEFAULT_COMMISSION_RATE);
    }

    /**
     * Admin updates the platform commission rate. Writes an immutable audit log.
     */
    public CommissionSetting updateCommissionRate(BigDecimal newRate, String notes, User admin) {
        if (newRate == null || newRate.compareTo(BigDecimal.ZERO) < 0 || newRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Commission rate must be between 0.0000 (0%) and 1.0000 (100%).");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal oldRate = getActiveCommissionRate();

        // Expire previous active setting
        settingRepository.findActiveSetting(now).ifPresent(s -> {
            s.setActive(false);
            s.setEffectiveTo(now);
            settingRepository.save(s);
        });

        CommissionSetting newSetting = new CommissionSetting(newRate, now, notes, admin);
        CommissionSetting saved = settingRepository.save(newSetting);

        auditService.log(
                admin.getId(), admin.getEmail(), "COMMISSION_RATE_UPDATED",
                "CommissionSetting", saved.getId(), "RATE_CHANGE", newRate,
                oldRate.toPlainString(), newRate.toPlainString(), notes, null
        );

        return saved;
    }

    /**
     * Computes and creates an immutable commission record snapshot for a paid order.
     */
    public CommissionRecord createCommissionSnapshot(Order order, PaymentTransaction payTx) {
        if (recordRepository.findByOrderId(order.getId()).isPresent()) {
            return recordRepository.findByOrderId(order.getId()).get();
        }

        BigDecimal rate = getActiveCommissionRate();
        BigDecimal grossAmount = order.getTotalAmount();

        // Commission = Gross * Rate (rounded to 2 decimal places)
        BigDecimal commissionAmount = grossAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal partnerFee = BigDecimal.ZERO;
        BigDecimal sellerNet = grossAmount.subtract(commissionAmount).subtract(partnerFee);

        User seller = order.getEffectiveSeller();
        if (seller == null) {
            throw new IllegalStateException("Order #" + order.getOrderNumber() + " has no associated seller.");
        }

        CommissionRecord record = new CommissionRecord(
                order, seller, payTx, grossAmount, rate, commissionAmount, partnerFee, sellerNet
        );
        CommissionRecord saved = recordRepository.save(record);

        auditService.log(
                null, "SYSTEM", "COMMISSION_SNAPSHOT_CREATED",
                "CommissionRecord", saved.getId(), order.getOrderNumber(), commissionAmount,
                null, "PENDING_SETTLEMENT",
                "Rate: " + rate + ", Gross: " + grossAmount + ", SellerNet: " + sellerNet, null
        );

        return saved;
    }

    /**
     * Marks commission as officially SETTLED upon successful payout confirmation.
     */
    public CommissionRecord settleCommission(Long orderId) {
        CommissionRecord record = recordRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("No commission record found for order ID: " + orderId));

        if (record.getStatus() == CommissionStatus.SETTLED) {
            return record;
        }

        record.setStatus(CommissionStatus.SETTLED);
        record.setSettledAt(LocalDateTime.now());
        CommissionRecord saved = recordRepository.save(record);

        auditService.log(
                null, "SYSTEM", "COMMISSION_SETTLED",
                "CommissionRecord", saved.getId(), record.getOrder().getOrderNumber(),
                saved.getCommissionAmount(), "PENDING_SETTLEMENT", "SETTLED", null, null
        );

        return saved;
    }

    /**
     * Reverses commission when an order is refunded.
     */
    public CommissionRecord reverseCommission(Long orderId, String reason) {
        CommissionRecord record = recordRepository.findByOrderId(orderId).orElse(null);
        if (record == null) {
            return null;
        }

        CommissionStatus oldStatus = record.getStatus();
        record.setStatus(CommissionStatus.REVERSED);
        CommissionRecord saved = recordRepository.save(record);

        auditService.log(
                null, "SYSTEM", "COMMISSION_REVERSED",
                "CommissionRecord", saved.getId(), record.getOrder().getOrderNumber(),
                saved.getCommissionAmount(), oldStatus.name(), "REVERSED", reason, null
        );

        return saved;
    }
}

