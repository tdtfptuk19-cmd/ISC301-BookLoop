package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.dto.AdminFinanceDashboardDto;
import com.bookloop.dto.ReconciliationItemDto;
import com.bookloop.dto.ReconciliationReportDto;
import com.bookloop.entity.*;
import com.bookloop.service.AdminFinanceService;
import com.bookloop.service.ReconciliationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for BookLoop Admin Finance Dashboard, offering complete oversight,
 * reconciliation, dispute mediation, payout retries, and commission configuration.
 */
@Controller
@RequestMapping("/admin/finance")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFinanceController {

    private final AdminFinanceService adminFinanceService;
    private final ReconciliationService reconciliationService;
    private final AuthenticatedUserResolver authResolver;

    public AdminFinanceController(AdminFinanceService adminFinanceService,
                                  ReconciliationService reconciliationService,
                                  AuthenticatedUserResolver authResolver) {
        this.adminFinanceService = adminFinanceService;
        this.reconciliationService = reconciliationService;
        this.authResolver = authResolver;
    }

    /**
     * Financial Overview Dashboard with KPIs and quick links.
     */
    @GetMapping
    public String showOverview(@RequestParam(value = "range", required = false, defaultValue = "ALL") String range,
                               @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                               @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                               Model model) {
        AdminFinanceDashboardDto kpis = adminFinanceService.getDashboardKpis(range, startDate, endDate);
        CommissionSetting commissionSetting = adminFinanceService.getActiveCommissionSetting();
        ReconciliationReportDto quickRecon = reconciliationService.runReconciliation("System Overview", false);

        model.addAttribute("kpis", kpis);
        model.addAttribute("commissionSetting", commissionSetting);
        model.addAttribute("reconciliation", quickRecon);
        model.addAttribute("recentPayments", adminFinanceService.getPayments(null, PageRequest.of(0, 5)).getContent());
        model.addAttribute("recentPayouts", adminFinanceService.getPayouts(null, PageRequest.of(0, 5)).getContent());
        model.addAttribute("activeDisputes", adminFinanceService.getDisputes(DisputeStatus.OPEN, PageRequest.of(0, 5)).getContent());
        model.addAttribute("selectedRange", range != null ? range.toUpperCase() : "ALL");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/finance/overview";
    }

    /**
     * Payment Transactions listing.
     */
    @GetMapping("/payments")
    public String listPayments(@RequestParam(value = "status", required = false) PaymentStatus status,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        model.addAttribute("payments", adminFinanceService.getPayments(status, pageable));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", PaymentStatus.values());
        return "admin/finance/payments";
    }

    /**
     * Seller Payout Disbursements listing.
     */
    @GetMapping("/payouts")
    public String listPayouts(@RequestParam(value = "status", required = false) PayoutStatus status,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        model.addAttribute("payouts", adminFinanceService.getPayouts(status, pageable));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", PayoutStatus.values());
        return "admin/finance/payouts";
    }

    /**
     * Platform Commissions listing.
     */
    @GetMapping("/commissions")
    public String listCommissions(@RequestParam(value = "status", required = false) CommissionStatus status,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        model.addAttribute("commissions", adminFinanceService.getCommissions(status, pageable));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", CommissionStatus.values());
        return "admin/finance/commissions";
    }

    /**
     * Buyer Refunds listing.
     */
    @GetMapping("/refunds")
    public String listRefunds(@RequestParam(value = "status", required = false) RefundStatus status,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        model.addAttribute("refunds", adminFinanceService.getRefunds(status, pageable));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", RefundStatus.values());
        return "admin/finance/refunds";
    }

    /**
     * Disputes Mediation listing.
     */
    @GetMapping("/disputes")
    public String listDisputes(@RequestParam(value = "status", required = false) DisputeStatus status,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        model.addAttribute("disputes", adminFinanceService.getDisputes(status, pageable));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", DisputeStatus.values());
        return "admin/finance/disputes";
    }

    /**
     * Reconciliation Auditor screen with filtering.
     */
    @GetMapping("/reconciliation")
    public String runReconciliation(@RequestParam(value = "status", required = false) String status,
                                    @RequestParam(value = "keyword", required = false) String keyword,
                                    Model model) {
        User admin = authResolver.getCurrentUser();
        String auditor = admin != null ? admin.getEmail() : "admin@bookloop.vn";
        ReconciliationReportDto report = reconciliationService.runReconciliation(auditor, true);

        List<ReconciliationItemDto> filteredItems = report.getFilteredItems(status, keyword);

        model.addAttribute("report", report);
        model.addAttribute("reconciliationItems", filteredItems);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statuses", ReconciliationStatus.values());

        return "admin/finance/reconciliation";
    }

    /**
     * Safe CSV Export for financial reconciliation audit.
     * Never exposes credentials or secrets. Prefixes fields to prevent CSV formula injection.
     */
    @GetMapping("/reconciliation/export")
    public void exportReconciliationCsv(@RequestParam(value = "status", required = false) String status,
                                        @RequestParam(value = "keyword", required = false) String keyword,
                                        HttpServletResponse response) throws IOException {
        User admin = authResolver.getCurrentUser();
        String auditor = admin != null ? admin.getEmail() : "admin@bookloop.vn";
        ReconciliationReportDto report = reconciliationService.runReconciliation(auditor, false);
        List<ReconciliationItemDto> items = report.getFilteredItems(status, keyword);

        String filename = "bookloop-reconciliation-report-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        // Write UTF-8 BOM so Microsoft Excel renders Vietnamese correctly
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        PrintWriter writer = new PrintWriter(response.getOutputStream(), true, StandardCharsets.UTF_8);
        writer.println("Mã đơn hàng,Mã giao dịch cổng,Cổng thanh toán,Phương thức TT,Giá trị đơn (₫),Đã thanh toán (₫),Hoa hồng sàn (₫),Giải ngân người bán (₫),Hoàn tiền (₫),Chênh lệch (₫),Trạng thái đối soát,Ghi chú kiểm toán,Thời gian giao dịch");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (ReconciliationItemDto it : items) {
            String orderNum = sanitizeCsv(it.getOrderNumber());
            String provRef = sanitizeCsv(it.getProviderReference());
            String gateway = "DEMO / MOCK GATEWAY";
            String method = sanitizeCsv(it.getPaymentMethod());
            String gross = it.getOrderGrossAmount() != null ? it.getOrderGrossAmount().toPlainString() : "0";
            String paid = it.getPaymentAmount() != null ? it.getPaymentAmount().toPlainString() : "0";
            String comm = it.getCommissionAmount() != null ? it.getCommissionAmount().toPlainString() : "0";
            String payout = it.getPayoutAmount() != null ? it.getPayoutAmount().toPlainString() : "0";
            String refund = it.getRefundAmount() != null ? it.getRefundAmount().toPlainString() : "0";
            String diff = it.getDiscrepancyAmount() != null ? it.getDiscrepancyAmount().toPlainString() : "0";
            String st = it.getStatus() != null ? it.getStatus().getDisplayName() : "PENDING";
            String notes = sanitizeCsv(it.getNotes());
            String date = it.getTransactionDate() != null ? it.getTransactionDate().format(dtf) : "N/A";

            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s,%s,%s,%s,\"%s\",\"%s\",\"%s\"%n",
                    orderNum, provRef, gateway, method, gross, paid, comm, payout, refund, diff, st, notes, date);
        }
        writer.flush();
    }

    private String sanitizeCsv(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.startsWith("=") || s.startsWith("+") || s.startsWith("-") || s.startsWith("@")) {
            s = "'" + s;
        }
        return s.replace("\"", "\"\"");
    }

    /**
     * Audit Trail listing.
     */
    @GetMapping("/audit-logs")
    public String listAuditLogs(@RequestParam(value = "page", defaultValue = "0") int page,
                                Model model) {
        Pageable pageable = PageRequest.of(page, 20);
        model.addAttribute("auditLogs", adminFinanceService.getAuditLogs(pageable));
        return "admin/finance/audit-logs";
    }

    /**
     * Retries a failed payout disbursement.
     */
    @PostMapping("/payouts/{id}/retry")
    public String retryPayout(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User admin = authResolver.getCurrentUser();
        try {
            adminFinanceService.retryPayout(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi lại lệnh giải ngân thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể gửi lại lệnh giải ngân: " + ex.getMessage());
        }
        return "redirect:/admin/finance/payouts";
    }

    /**
     * Mediates and resolves a buyer dispute.
     */
    @PostMapping("/disputes/{id}/resolve")
    public String resolveDispute(@PathVariable Long id,
                                 @RequestParam("resolution") DisputeStatus resolution,
                                 @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
                                 @RequestParam(value = "adminNotes", required = false) String adminNotes,
                                 RedirectAttributes redirectAttributes) {
        User admin = authResolver.getCurrentUser();
        try {
            adminFinanceService.resolveDispute(id, resolution, refundAmount, adminNotes, admin);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xử lý tranh chấp ID #" + id + " thành công theo hướng: " + resolution.name());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi xử lý tranh chấp: " + ex.getMessage());
        }
        return "redirect:/admin/finance/disputes";
    }

    /**
     * Updates platform commission rates.
     */
    @PostMapping("/commission-settings")
    public String updateCommissionSettings(@RequestParam("ratePercentage") BigDecimal ratePercentage,
                                           @RequestParam(value = "fixedFee", defaultValue = "0") BigDecimal fixedFee,
                                           RedirectAttributes redirectAttributes) {
        User admin = authResolver.getCurrentUser();
        try {
            adminFinanceService.updateCommissionSetting(ratePercentage, fixedFee, admin.getEmail());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã cập nhật mức phí chiết khấu hoa hồng thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi cập nhật cấu hình hoa hồng: " + ex.getMessage());
        }
        return "redirect:/admin/finance";
    }
}
