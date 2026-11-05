package com.bookloop.util;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Reusable Vietnamese localization formatter for currency and dates.
 * Accessible in Thymeleaf via {@code @formatUtils.formatVnd(amount)}.
 */
@Component("formatUtils")
public class FormatUtils {

    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FormatUtils() {}

    /**
     * Formats a BigDecimal amount as Vietnamese Dong (e.g. 50.000 ₫).
     */
    public static String formatVnd(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(VI_LOCALE);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0", symbols);
        return df.format(amount) + " ₫";
    }

    /**
     * Formats a number as VND with amount.
     */
    public static String formatVnd(long amount) {
        return formatVnd(BigDecimal.valueOf(amount));
    }

    /**
     * Formats a number as VND with amount.
     */
    public static String formatVnd(double amount) {
        return formatVnd(BigDecimal.valueOf(amount));
    }

    /**
     * Formats date as dd/MM/yyyy.
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "—";
        return dateTime.format(DATE_FMT);
    }

    /**
     * Formats datetime as dd/MM/yyyy HH:mm.
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "—";
        return dateTime.format(DATETIME_FMT);
    }
}
