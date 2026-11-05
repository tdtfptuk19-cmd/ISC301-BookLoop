package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.dto.ListingModerationForm;
import com.bookloop.dto.ReportResolutionForm;
import com.bookloop.entity.*;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Admin Dashboard controller — all routes under /admin/** require ROLE_ADMIN.
 * Access control is enforced by SecurityConfig.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final int PAGE_SIZE = 20;

    private final AdminService adminService;
    private final AuthenticatedUserResolver userResolver;
    private final com.bookloop.service.PromotionService promotionService;
    private final com.bookloop.service.PlatformSettingService settingService;
    private final com.bookloop.service.CategoryService categoryService;

    public AdminController(AdminService adminService,
                           AuthenticatedUserResolver userResolver,
                           com.bookloop.service.PromotionService promotionService,
                           com.bookloop.service.PlatformSettingService settingService,
                           com.bookloop.service.CategoryService categoryService) {
        this.adminService = adminService;
        this.userResolver = userResolver;
        this.promotionService = promotionService;
        this.settingService = settingService;
        this.categoryService = categoryService;
    }

    // =========================================================================
    // DASHBOARD
    // =========================================================================

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        Map<String, Object> activity = adminService.getRecentActivity();
        model.addAttribute("recentUsers", activity.get("recentUsers"));
        model.addAttribute("recentListings", activity.get("recentListings"));
        model.addAttribute("recentOrders", activity.get("recentOrders"));
        model.addAttribute("recentReports", activity.get("recentReports"));
        return "admin/dashboard";
    }

    // =========================================================================
    // USER MANAGEMENT
    // =========================================================================

    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) String enabled,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        Role roleFilter = null;
        if (role != null && !role.isBlank()) {
            try { roleFilter = Role.valueOf(role.toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }
        Boolean enabledFilter = null;
        if ("true".equalsIgnoreCase(enabled)) enabledFilter = true;
        else if ("false".equalsIgnoreCase(enabled)) enabledFilter = false;

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<User> users = adminService.getAllUsers(keyword, roleFilter, enabledFilter, pageable);

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedEnabled", enabled);
        model.addAttribute("roles", Role.values());
        return "admin/users/list";
    }

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", adminService.getUserById(id));
        return "admin/users/detail";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id,
                                   @RequestParam boolean enable,
                                   RedirectAttributes redirectAttributes) {
        User adminUser = userResolver.getCurrentUser();
        try {
            adminService.toggleUserEnabled(id, enable, adminUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User account has been " + (enable ? "activated" : "deactivated") + ".");
        } catch (UnauthorizedOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/change-role")
    public String changeUserRole(@PathVariable Long id,
                                 @RequestParam String newRole,
                                 RedirectAttributes redirectAttributes) {
        User adminUser = userResolver.getCurrentUser();
        try {
            Role role = Role.valueOf(newRole.toUpperCase());
            adminService.changeUserRole(id, role, adminUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User role updated to " + role.name() + ".");
        } catch (UnauthorizedOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid role: " + newRole);
        }
        return "redirect:/admin/users/" + id;
    }

    // =========================================================================
    // LISTING MANAGEMENT
    // =========================================================================

    @GetMapping("/listings")
    public String listListings(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String category,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Product> listings = adminService.getAllProducts(keyword, status, category, pageable);

        model.addAttribute("listings", listings);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("statuses", ListingStatus.values());
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("moderationForm", new ListingModerationForm());
        return "admin/listings/list";
    }

    @GetMapping("/listings/{id}")
    public String viewListing(@PathVariable Long id, Model model) {
        model.addAttribute("product", adminService.getProductById(id));
        model.addAttribute("moderationForm", new ListingModerationForm());
        return "admin/listings/detail";
    }

    @PostMapping("/listings/{id}/hide")
    public String hideListing(@PathVariable Long id,
                              @ModelAttribute ListingModerationForm form,
                              RedirectAttributes redirectAttributes) {
        User adminUser = userResolver.getCurrentUser();
        try {
            adminService.hideProduct(id, form.getModerationNote(), adminUser);
            redirectAttributes.addFlashAttribute("successMessage", "Listing has been hidden.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/listings/" + id;
    }

    @PostMapping("/listings/{id}/unhide")
    public String unhideListing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User adminUser = userResolver.getCurrentUser();
        try {
            adminService.unhideProduct(id, adminUser);
            redirectAttributes.addFlashAttribute("successMessage", "Listing has been restored to AVAILABLE.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/listings/" + id;
    }

    // =========================================================================
    // ORDER MANAGEMENT (read-only)
    // =========================================================================

    @GetMapping("/orders")
    public String listOrders(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String status,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Order> orders = adminService.getAllOrders(keyword, status, pageable);

        model.addAttribute("orders", orders);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/list";
    }

    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        model.addAttribute("order", adminService.getOrderById(id));
        return "admin/orders/detail";
    }

    // =========================================================================
    // REVIEW MANAGEMENT
    // =========================================================================

    @GetMapping("/reviews")
    public String listReviews(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer rating,
                              @RequestParam(required = false) String hidden,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        Boolean hiddenFilter = null;
        if ("true".equalsIgnoreCase(hidden)) hiddenFilter = true;
        else if ("false".equalsIgnoreCase(hidden)) hiddenFilter = false;

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Review> reviews = adminService.getAllReviews(keyword, rating, hiddenFilter, pageable);

        model.addAttribute("reviews", reviews);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRating", rating);
        model.addAttribute("selectedHidden", hidden);
        return "admin/reviews/list";
    }

    @PostMapping("/reviews/{id}/hide")
    public String hideReview(@PathVariable Long id,
                             @RequestParam(defaultValue = "0") int page,
                             RedirectAttributes redirectAttributes) {
        User adminUser = userResolver.getCurrentUser();
        try {
            adminService.hideReview(id, adminUser);
            redirectAttributes.addFlashAttribute("successMessage", "Review hidden.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reviews?page=" + page;
    }

    @PostMapping("/reviews/{id}/unhide")
    public String unhideReview(@PathVariable Long id,
                               @RequestParam(defaultValue = "0") int page,
                               RedirectAttributes redirectAttributes) {
        User adminUser = userResolver.getCurrentUser();
        try {
            adminService.unhideReview(id, adminUser);
            redirectAttributes.addFlashAttribute("successMessage", "Review restored.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reviews?page=" + page;
    }

    // =========================================================================
    // REPORT MANAGEMENT
    // =========================================================================

    @GetMapping("/reports")
    public String listReports(@RequestParam(required = false) String status,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Report> reports = adminService.getAllReports(status, pageable);

        model.addAttribute("reports", reports);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("reportStatuses", ReportStatus.values());
        return "admin/reports/list";
    }

    @GetMapping("/reports/{id}")
    public String viewReport(@PathVariable Long id, Model model) {
        model.addAttribute("report", adminService.getReportById(id));
        model.addAttribute("resolutionForm", new ReportResolutionForm());
        model.addAttribute("reportStatuses", ReportStatus.values());
        return "admin/reports/detail";
    }

    @PostMapping("/reports/{id}/resolve")
    public String resolveReport(@PathVariable Long id,
                                @ModelAttribute ReportResolutionForm form,
                                RedirectAttributes redirectAttributes) {
        User adminUser = userResolver.getCurrentUser();
        try {
            adminService.resolveReport(id, form.getNewStatus(), form.getAdminNote(), adminUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Report updated to " + form.getNewStatus() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reports/" + id;
    }

    // =========================================================================
    // PROMOTION FEE & PAYMENT MANAGEMENT
    // =========================================================================

    @GetMapping("/promotion-payments")
    public String listPromotionPayments(@RequestParam(required = false) String status,
                                        @RequestParam(defaultValue = "0") int page,
                                        Model model) {
        PromotionPaymentStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try { statusFilter = PromotionPaymentStatus.valueOf(status.toUpperCase()); } catch (Exception ignored) {}
        }

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<PromotionPayment> payments = promotionService.getPaymentsForAdmin(statusFilter, pageable);

        model.addAttribute("payments", payments);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", PromotionPaymentStatus.values());
        return "admin/promotion-payments/list";
    }

    @PostMapping("/promotion-payments/{id}/verify")
    public String verifyPromotionPayment(@PathVariable Long id,
                                         @RequestParam boolean approved,
                                         @RequestParam(required = false) String adminNote,
                                         RedirectAttributes redirectAttributes) {
        User adminUser = userResolver.getCurrentUser();
        try {
            promotionService.verifyPayment(id, approved, adminNote, adminUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    approved ? "Đã duyệt thanh toán và kích hoạt quảng cáo cho tin đăng!" : "Đã từ chối thanh toán phí quảng cáo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/promotion-payments";
    }

    @GetMapping("/fee-settings")
    public String showFeeSettings(Model model) {
        model.addAttribute("currentFee", settingService.getListingPromotionFee());
        model.addAttribute("durationDays", settingService.getPromotionDurationDays());
        model.addAttribute("instructions", settingService.getPaymentInstructions());
        return "admin/fee-settings";
    }

    @PostMapping("/fee-settings")
    public String updateFeeSettings(@RequestParam java.math.BigDecimal fee,
                                    @RequestParam int durationDays,
                                    @RequestParam String instructions,
                                    RedirectAttributes redirectAttributes) {
        User adminUser = userResolver.getCurrentUser();
        try {
            settingService.setListingPromotionFee(fee);
            settingService.setPromotionDurationDays(durationDays);
            settingService.setPaymentInstructions(instructions);

            adminService.logAction(adminUser, "FEE_SETTINGS_UPDATED", "SETTING", null,
                    String.format("Cập nhật phí đăng tin: %s VND, thời hạn: %d ngày", fee.toPlainString(), durationDays));

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật cài đặt phí đăng tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/fee-settings";
    }

    // =========================================================================
    // ACTIVITY LOG
    // =========================================================================

    @GetMapping("/logs")
    public String viewLogs(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        model.addAttribute("logs", adminService.getActivityLogs(pageable));
        return "admin/logs/list";
    }
}

