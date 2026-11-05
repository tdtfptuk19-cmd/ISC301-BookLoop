package com.bookloop.service;

import com.bookloop.dto.AdminDashboardStats;
import com.bookloop.entity.*;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for Admin Dashboard operations.
 * Handles user management, listing moderation, report resolution,
 * review moderation, and audit logging.
 */
@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final AdminLogRepository adminLogRepository;

    public AdminService(UserRepository userRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        ReviewRepository reviewRepository,
                        ReportRepository reportRepository,
                        AdminLogRepository adminLogRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.adminLogRepository = adminLogRepository;
    }

    // =========================================================================
    // DASHBOARD
    // =========================================================================

    @Transactional(readOnly = true)
    public AdminDashboardStats getDashboardStats() {
        AdminDashboardStats stats = new AdminDashboardStats();
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByEnabled(true));
        stats.setTotalListings(productRepository.count());
        stats.setAvailableListings(productRepository.countByStatus(ListingStatus.AVAILABLE));
        stats.setSoldListings(productRepository.countByStatus(ListingStatus.SOLD));
        stats.setHiddenListings(productRepository.countByStatus(ListingStatus.HIDDEN));
        stats.setTotalOrders(orderRepository.count());
        stats.setCompletedOrders(orderRepository.countByStatus(OrderStatus.COMPLETED));
        stats.setPendingOrders(orderRepository.countByStatus(OrderStatus.PENDING));
        stats.setPendingReports(reportRepository.countByStatus(ReportStatus.PENDING));
        stats.setTotalReviews(reviewRepository.count());
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRecentActivity() {
        Map<String, Object> activity = new HashMap<>();
        activity.put("recentUsers", userRepository.findTop5ByOrderByCreatedAtDesc());
        activity.put("recentListings", productRepository.findTop5ByOrderByCreatedAtDesc());
        activity.put("recentOrders", orderRepository.findTop5ByOrderByOrderDateDesc());
        activity.put("recentReports", reportRepository.findAllOrderByCreatedAtDesc(PageRequest.of(0, 5)).getContent());
        return activity;
    }

    // =========================================================================
    // USER MANAGEMENT
    // =========================================================================

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(String keyword, Role role, Boolean enabled, Pageable pageable) {
        return userRepository.searchUsers(
                (keyword != null && keyword.isBlank()) ? null : keyword,
                role,
                enabled,
                pageable);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    /**
     * Toggle a user's enabled status. Prevents deactivating the last active admin.
     */
    public void toggleUserEnabled(Long userId, boolean newEnabled, User adminUser) {
        User target = getUserById(userId);

        if (!newEnabled && target.getRole() == Role.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndEnabled(Role.ADMIN, true);
            if (activeAdminCount <= 1) {
                throw new UnauthorizedOperationException(
                        "Cannot deactivate the only active administrator account.");
            }
        }

        target.setEnabled(newEnabled);
        userRepository.save(target);

        logAction(adminUser,
                newEnabled ? "USER_ACTIVATED" : "USER_DEACTIVATED",
                "USER", userId,
                String.format("Admin %s %s user account for %s (id=%d)",
                        adminUser.getFullName(),
                        newEnabled ? "activated" : "deactivated",
                        target.getFullName(), userId));
    }

    /**
     * Change a user's role. Prevents removing the last admin.
     */
    public void changeUserRole(Long userId, Role newRole, User adminUser) {
        User target = getUserById(userId);

        if (target.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndEnabled(Role.ADMIN, true);
            if (activeAdminCount <= 1 && target.isEnabled()) {
                throw new UnauthorizedOperationException(
                        "Cannot remove the only active administrator's role.");
            }
        }

        Role oldRole = target.getRole();
        target.setRole(newRole);
        userRepository.save(target);

        logAction(adminUser, "USER_ROLE_CHANGED", "USER", userId,
                String.format("Admin %s changed role of %s from %s to %s",
                        adminUser.getFullName(), target.getFullName(), oldRole, newRole));
    }

    // =========================================================================
    // LISTING MANAGEMENT
    // =========================================================================

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(String keyword, String statusStr, Pageable pageable) {
        return getAllProducts(keyword, statusStr, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(String keyword, String statusStr, String category, Pageable pageable) {
        ListingStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = ListingStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        String cleanCategory = (category != null && !category.isBlank()) ? category.trim() : null;
        return productRepository.searchAllForAdmin(
                (keyword != null && keyword.isBlank()) ? null : keyword,
                status, cleanCategory, pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + id));
    }

    /**
     * Hide a listing (admin moderation action).
     */
    public void hideProduct(Long productId, String moderationNote, User adminUser) {
        Product product = getProductById(productId);
        if (product.getStatus() == ListingStatus.SOLD) {
            throw new IllegalStateException("Cannot hide a SOLD listing.");
        }
        product.setStatus(ListingStatus.HIDDEN);
        product.setModerationNote(moderationNote);
        productRepository.save(product);

        logAction(adminUser, "LISTING_HIDDEN", "PRODUCT", productId,
                String.format("Admin %s hid listing '%s' (id=%d). Note: %s",
                        adminUser.getFullName(), product.getTitle(), productId, moderationNote));
    }

    /**
     * Restore a hidden listing to AVAILABLE.
     */
    public void unhideProduct(Long productId, User adminUser) {
        Product product = getProductById(productId);
        if (product.getStatus() != ListingStatus.HIDDEN) {
            throw new IllegalStateException("Listing is not currently hidden.");
        }
        product.setStatus(ListingStatus.AVAILABLE);
        product.setModerationNote(null);
        productRepository.save(product);

        logAction(adminUser, "LISTING_RESTORED", "PRODUCT", productId,
                String.format("Admin %s restored listing '%s' (id=%d) to AVAILABLE.",
                        adminUser.getFullName(), product.getTitle(), productId));
    }

    // =========================================================================
    // ORDER MANAGEMENT (read-only inspection + view)
    // =========================================================================

    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(String keyword, String statusStr, Pageable pageable) {
        OrderStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = OrderStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return orderRepository.searchAllForAdmin(
                (keyword != null && keyword.isBlank()) ? null : keyword,
                status, pageable);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    // =========================================================================
    // REVIEW MANAGEMENT
    // =========================================================================

    @Transactional(readOnly = true)
    public Page<Review> getAllReviews(String keyword, Integer rating, Boolean hidden, Pageable pageable) {
        return reviewRepository.searchReviews(
                (keyword != null && keyword.isBlank()) ? null : keyword,
                rating, hidden, pageable);
    }

    @Transactional(readOnly = true)
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));
    }

    public void hideReview(Long reviewId, User adminUser) {
        Review review = getReviewById(reviewId);
        review.setHidden(true);
        reviewRepository.save(review);

        logAction(adminUser, "REVIEW_HIDDEN", "REVIEW", reviewId,
                String.format("Admin %s hid review id=%d.", adminUser.getFullName(), reviewId));
    }

    public void unhideReview(Long reviewId, User adminUser) {
        Review review = getReviewById(reviewId);
        review.setHidden(false);
        reviewRepository.save(review);

        logAction(adminUser, "REVIEW_RESTORED", "REVIEW", reviewId,
                String.format("Admin %s restored review id=%d.", adminUser.getFullName(), reviewId));
    }

    // =========================================================================
    // REPORT MANAGEMENT
    // =========================================================================

    @Transactional(readOnly = true)
    public Page<Report> getAllReports(String statusStr, Pageable pageable) {
        ReportStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = ReportStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (status == null) {
            return reportRepository.findAllOrderByCreatedAtDesc(pageable);
        }
        return reportRepository.findAllWithFilters(status, pageable);
    }

    @Transactional(readOnly = true)
    public Report getReportById(Long id) {
        return reportRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));
    }

    public void resolveReport(Long reportId, ReportStatus newStatus, String adminNote, User adminUser) {
        Report report = getReportById(reportId);
        report.setStatus(newStatus);
        report.setAdminNote(adminNote);
        report.setHandledBy(adminUser);
        reportRepository.save(report);

        logAction(adminUser, "REPORT_RESOLVED", "REPORT", reportId,
                String.format("Admin %s set report id=%d to %s.", adminUser.getFullName(), reportId, newStatus));
    }

    // =========================================================================
    // ACTIVITY LOG
    // =========================================================================

    @Transactional(readOnly = true)
    public Page<AdminLog> getActivityLogs(Pageable pageable) {
        return adminLogRepository.findAllOrderByCreatedAtDesc(pageable);
    }

    public void logAction(User admin, String action, String targetType, Long targetId, String description) {
        AdminLog log = new AdminLog(admin, action, targetType, targetId, description);
        adminLogRepository.save(log);
    }
}

