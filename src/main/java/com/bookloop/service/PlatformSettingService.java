package com.bookloop.service;

import com.bookloop.entity.PlatformSetting;
import com.bookloop.repository.PlatformSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages dynamic platform settings (promotion fees, duration, bank transfer instructions).
 */
@Service
@Transactional
public class PlatformSettingService {

    public static final String KEY_PROMOTION_FEE = "LISTING_PROMOTION_FEE";
    public static final String KEY_PROMOTION_DURATION = "PROMOTION_DURATION_DAYS";
    public static final String KEY_PAYMENT_INSTRUCTIONS = "PAYMENT_INSTRUCTIONS";

    public static final BigDecimal DEFAULT_FEE = new BigDecimal("10000.00");
    public static final int DEFAULT_DURATION = 7;
    public static final String DEFAULT_INSTRUCTIONS = """
            Ngân hàng: Ngân hàng TMCP Ngoại thương Việt Nam (Vietcombank)
            Chủ tài khoản: NGUYEN VAN A (BOOKLOOP DEMO)
            Số tài khoản: 0011001234567
            Nội dung chuyển khoản: [MÃ GIAO DỊCH] - [SỐ ĐIỆN THOẠI]
            Lưu ý: Sau khi chuyển khoản, vui lòng điền mã tham chiếu bên dưới để Quản trị viên duyệt nhanh chóng.""";

    private final PlatformSettingRepository settingRepository;

    public PlatformSettingService(PlatformSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Transactional(readOnly = true)
    public BigDecimal getListingPromotionFee() {
        return settingRepository.findById(KEY_PROMOTION_FEE)
                .map(s -> {
                    try {
                        return new BigDecimal(s.getSettingValue());
                    } catch (Exception e) {
                        return DEFAULT_FEE;
                    }
                })
                .orElse(DEFAULT_FEE);
    }

    public void setListingPromotionFee(BigDecimal fee) {
        saveSetting(KEY_PROMOTION_FEE, fee.toPlainString(), "Phí đăng tin và quảng cáo sách (VND)");
    }

    @Transactional(readOnly = true)
    public int getPromotionDurationDays() {
        return settingRepository.findById(KEY_PROMOTION_DURATION)
                .map(s -> {
                    try {
                        return Integer.parseInt(s.getSettingValue());
                    } catch (Exception e) {
                        return DEFAULT_DURATION;
                    }
                })
                .orElse(DEFAULT_DURATION);
    }

    public void setPromotionDurationDays(int days) {
        saveSetting(KEY_PROMOTION_DURATION, String.valueOf(days), "Thời hạn quảng cáo tin đăng (ngày)");
    }

    @Transactional(readOnly = true)
    public String getPaymentInstructions() {
        return settingRepository.findById(KEY_PAYMENT_INSTRUCTIONS)
                .map(PlatformSetting::getSettingValue)
                .orElse(DEFAULT_INSTRUCTIONS);
    }

    public void setPaymentInstructions(String instructions) {
        saveSetting(KEY_PAYMENT_INSTRUCTIONS, instructions, "Hướng dẫn chuyển khoản thanh toán phí");
    }

    @Transactional(readOnly = true)
    public Map<String, String> getAllSettings() {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_PROMOTION_FEE, getListingPromotionFee().toPlainString());
        map.put(KEY_PROMOTION_DURATION, String.valueOf(getPromotionDurationDays()));
        map.put(KEY_PAYMENT_INSTRUCTIONS, getPaymentInstructions());
        return map;
    }

    private void saveSetting(String key, String value, String desc) {
        PlatformSetting s = settingRepository.findById(key)
                .orElseGet(() -> new PlatformSetting(key, value, desc));
        s.setSettingValue(value);
        s.setDescription(desc);
        s.setUpdatedAt(LocalDateTime.now());
        settingRepository.save(s);
    }
}

