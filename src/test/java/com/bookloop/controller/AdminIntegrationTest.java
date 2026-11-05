package com.bookloop.controller;

import com.bookloop.entity.*;
import com.bookloop.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test suite for Phase 5: Admin Dashboard & Platform Management.
 * Validates RBAC, user management, listing moderation, dispute reporting,
 * review hiding, and audit logging.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AdminLogRepository adminLogRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private com.bookloop.repository.PromotionPaymentRepository promotionPaymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User studentBuyer;
    private User studentSeller;
    private Product activeProduct;
    private Product soldProduct;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        // 1. Create Admin
        adminUser = new User();
        adminUser.setFullName("Platform Admin");
        adminUser.setEmail("admin@bookloop.edu");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole(Role.ADMIN);
        adminUser.setEnabled(true);
        adminUser = userRepository.save(adminUser);

        // 2. Create Student Buyer
        studentBuyer = new User();
        studentBuyer.setFullName("Student Buyer");
        studentBuyer.setEmail("student.buyer@university.edu");
        studentBuyer.setPassword(passwordEncoder.encode("pass123"));
        studentBuyer.setRole(Role.STUDENT);
        studentBuyer.setEnabled(true);
        studentBuyer = userRepository.save(studentBuyer);

        // 3. Create Student Seller
        studentSeller = new User();
        studentSeller.setFullName("Student Seller");
        studentSeller.setEmail("student.seller@university.edu");
        studentSeller.setPassword(passwordEncoder.encode("pass123"));
        studentSeller.setRole(Role.STUDENT);
        studentSeller.setEnabled(true);
        studentSeller = userRepository.save(studentSeller);

        // 4. Create Active Product
        activeProduct = new Product();
        activeProduct.setTitle("Computer Networks 5th Edition");
        activeProduct.setAuthor("Andrew S. Tanenbaum");
        activeProduct.setPrice(new BigDecimal("45.00"));
        activeProduct.setCondition(Condition.GOOD);
        activeProduct.setCategory("Computer Science");
        activeProduct.setCampus("Main Campus");
        activeProduct.setStatus(ListingStatus.AVAILABLE);
        activeProduct.setSeller(studentSeller);
        activeProduct = productRepository.save(activeProduct);

        // 5. Create Sold Product
        soldProduct = new Product();
        soldProduct.setTitle("Operating Systems Design");
        soldProduct.setAuthor("Tanenbaum");
        soldProduct.setPrice(new BigDecimal("50.00"));
        soldProduct.setCondition(Condition.LIKE_NEW);
        soldProduct.setCategory("Computer Science");
        soldProduct.setCampus("Main Campus");
        soldProduct.setStatus(ListingStatus.SOLD);
        soldProduct.setSeller(studentSeller);
        soldProduct = productRepository.save(soldProduct);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        adminLogRepository.deleteAll();
        reportRepository.deleteAll();
        reviewRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        promotionPaymentRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    // =========================================================================
    // 1. RBAC & SECURITY
    // =========================================================================

    @Test
    @DisplayName("Admin Dashboard: Anonymous user is redirected to login")
    void dashboard_anonymousRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student.buyer@university.edu", roles = {"STUDENT"})
    @DisplayName("Admin Dashboard: Regular student user receives 403 Forbidden")
    void dashboard_studentForbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@bookloop.edu", roles = {"ADMIN"})
    @DisplayName("Admin Dashboard: Admin accesses dashboard with statistics and recent activity")
    void dashboard_adminSuccess() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("recentUsers"))
                .andExpect(model().attributeExists("recentListings"));
    }

    // =========================================================================
    // 2. USER MANAGEMENT
    // =========================================================================

    @Test
    @WithMockUser(username = "admin@bookloop.edu", roles = {"ADMIN"})
    @DisplayName("User Management: Admin views paginated user list with filters")
    void users_listAndSearch() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("keyword", "Buyer")
                        .param("role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/list"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    @WithMockUser(username = "admin@bookloop.edu", roles = {"ADMIN"})
    @DisplayName("User Management: Admin can deactivate and reactivate student account")
    void users_deactivateAndActivate() throws Exception {
        // Deactivate student
        mockMvc.perform(post("/admin/users/{id}/toggle-status", studentBuyer.getId())
                        .with(csrf())
                        .param("enable", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/" + studentBuyer.getId()));

        User updated = userRepository.findById(studentBuyer.getId()).orElseThrow();
        assertThat(updated.isEnabled()).isFalse();

        // Audit log created
        assertThat(adminLogRepository.count()).isGreaterThan(0);

        // Reactivate student
        mockMvc.perform(post("/admin/users/{id}/toggle-status", studentBuyer.getId())
                        .with(csrf())
                        .param("enable", "true"))
                .andExpect(status().is3xxRedirection());

        updated = userRepository.findById(studentBuyer.getId()).orElseThrow();
        assertThat(updated.isEnabled()).isTrue();
    }

    @Test
    @WithMockUser(username = "admin@bookloop.edu", roles = {"ADMIN"})
    @DisplayName("User Management: Last-admin protection prevents deactivating the only admin")
    void users_cannotDeactivateOnlyAdmin() throws Exception {
        mockMvc.perform(post("/admin/users/{id}/toggle-status", adminUser.getId())
                        .with(csrf())
                        .param("enable", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));

        User stillAdmin = userRepository.findById(adminUser.getId()).orElseThrow();
        assertThat(stillAdmin.isEnabled()).isTrue();
    }

    @Test
    @WithMockUser(username = "admin@bookloop.edu", roles = {"ADMIN"})
    @DisplayName("User Management: Change user role from STUDENT to ADMIN and back")
    void users_changeRole() throws Exception {
        mockMvc.perform(post("/admin/users/{id}/change-role", studentBuyer.getId())
                        .with(csrf())
                        .param("newRole", "ADMIN"))
                .andExpect(status().is3xxRedirection());

        User promoted = userRepository.findById(studentBuyer.getId()).orElseThrow();
        assertThat(promoted.getRole()).isEqualTo(Role.ADMIN);
    }

    // =========================================================================
    // 3. LISTING MODERATION
    // =========================================================================

    @Test
    @WithMockUser(username = "admin@bookloop.edu", roles = {"ADMIN"})
    @DisplayName("Listing Moderation: Admin hides active listing with moderation note")
    void listings_hideAndRestore() throws Exception {
        mockMvc.perform(post("/admin/listings/{id}/hide", activeProduct.getId())
                        .with(csrf())
                        .param("moderationNote", "Policy violation: wrong course textbook edition"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/listings/" + activeProduct.getId()));

        Product hidden = productRepository.findById(activeProduct.getId()).orElseThrow();
        assertThat(hidden.getStatus()).isEqualTo(ListingStatus.HIDDEN);
        assertThat(hidden.getModerationNote()).contains("Policy violation");

        // Restore listing
        mockMvc.perform(post("/admin/listings/{id}/unhide", activeProduct.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Product restored = productRepository.findById(activeProduct.getId()).orElseThrow();
        assertThat(restored.getStatus()).isEqualTo(ListingStatus.AVAILABLE);
        assertThat(restored.getModerationNote()).isNull();
    }

    @Test
    @WithMockUser(username = "admin@bookloop.edu", roles = {"ADMIN"})
    @DisplayName("Listing Moderation: Admin cannot hide an already SOLD listing")
    void listings_cannotHideSoldListing() throws Exception {
        mockMvc.perform(post("/admin/listings/{id}/hide", soldProduct.getId())
                        .with(csrf())
                        .param("moderationNote", "Attempt to hide sold"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));

        Product unchanged = productRepository.findById(soldProduct.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(ListingStatus.SOLD);
    }

    // =========================================================================
    // 4. ORDER INSPECTION (READ-ONLY)
    // =========================================================================

    @Test
    @WithMockUser(username = "admin@bookloop.edu", roles = {"ADMIN"})
    @DisplayName("Order Management: Admin inspects order details (read-only)")
    void orders_inspectOrder() throws Exception {
        Order order = new Order();
        order.setOrderNumber("ORD-TEST-2026-001");
        order.setBuyer(studentBuyer);
        order.setTotalAmount(new BigDecimal("45.00"));
        order.setPaymentMethod(PaymentMethod.IN_PERSON_MEETUP);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order = orderRepository.save(order);

        OrderItem item = new OrderItem(order, activeProduct, studentSeller, new BigDecimal("45.00"), 1);
        order.addItem(item);
        orderRepository.save(order);

        mockMvc.perform(get("/admin/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders/detail"))
                .andExpect(model().attributeExists("order"));
    }

    // =========================================================================
    // 5. REVIEW MODERATION
    // =========================================================================

    @Test
    @WithMockUser(username = "admin@bookloop.edu", roles = {"ADMIN"})
    @DisplayName("Review Moderation: Admin can hide inappropriate review")
    void reviews_hideReview() throws Exception {
        Order order = new Order();
        order.setOrderNumber("ORD-REV-001");
        order.setBuyer(studentBuyer);
        order.setTotalAmount(new BigDecimal("45.00"));
        order.setPaymentMethod(PaymentMethod.IN_PERSON_MEETUP);
        order.setStatus(OrderStatus.COMPLETED);
        order.setOrderDate(LocalDateTime.now());
        order = orderRepository.save(order);

        Review review = new Review(order, studentBuyer, studentSeller, 1, "Offensive remarks");
        review = reviewRepository.save(review);

        mockMvc.perform(post("/admin/reviews/{id}/hide", review.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Review updated = reviewRepository.findById(review.getId()).orElseThrow();
        assertThat(updated.isHidden()).isTrue();

        // Hidden review must NOT appear in public seller profile query
        var visibleReviews = reviewRepository.findBySellerIdOrderByCreatedAtDesc(studentSeller.getId());
        assertThat(visibleReviews).isEmpty();
    }

    // =========================================================================
    // 6. DISPUTE & LISTING REPORTS
    // =========================================================================

    @Test
    @WithMockUser(username = "student.buyer@university.edu", roles = {"STUDENT"})
    @DisplayName("Reports: Student submits report for suspicious listing")
    void reports_studentSubmitReport() throws Exception {
        mockMvc.perform(post("/reports")
                        .with(csrf())
                        .param("productId", activeProduct.getId().toString())
                        .param("reason", "Counterfeit or pirated book/PDF")
                        .param("description", "Seller uploaded scanned PDF rather than physical book."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + activeProduct.getId()));

        assertThat(reportRepository.count()).isEqualTo(1);
        Report report = reportRepository.findAll().get(0);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(report.getReason()).contains("Counterfeit");
    }

    @Test
    @WithMockUser(username = "student.seller@university.edu", roles = {"STUDENT"})
    @DisplayName("Reports: Seller cannot report their own listing")
    void reports_sellerCannotReportOwnListing() throws Exception {
        mockMvc.perform(post("/reports")
                        .with(csrf())
                        .param("productId", activeProduct.getId().toString())
                        .param("reason", "Other policy violation")
                        .param("description", "Self reporting attempt."))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/new"))
                .andExpect(model().attributeExists("errorMessage"));

        assertThat(reportRepository.count()).isEqualTo(0);
    }

    @Test
    @WithMockUser(username = "admin@bookloop.edu", roles = {"ADMIN"})
    @DisplayName("Reports: Admin resolves pending report with note")
    void reports_adminResolveReport() throws Exception {
        Report report = new Report();
        report.setReporter(studentBuyer);
        report.setReportedProduct(activeProduct);
        report.setReportedUser(studentSeller);
        report.setReason("Misleading condition");
        report.setStatus(ReportStatus.PENDING);
        report = reportRepository.save(report);

        mockMvc.perform(post("/admin/reports/{id}/resolve", report.getId())
                        .with(csrf())
                        .param("newStatus", "RESOLVED")
                        .param("adminNote", "Investigated with seller; condition was updated."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reports/" + report.getId()));

        Report resolved = reportRepository.findByIdWithDetails(report.getId()).orElseThrow();
        assertThat(resolved.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(resolved.getAdminNote()).contains("Investigated");
        assertThat(resolved.getHandledBy().getEmail()).isEqualTo("admin@bookloop.edu");
    }

    // =========================================================================
    // 7. AUDIT TRAIL
    // =========================================================================

    @Test
    @WithMockUser(username = "admin@bookloop.edu", roles = {"ADMIN"})
    @DisplayName("Audit Log: Admin can view immutable audit trail")
    void auditLog_viewLogs() throws Exception {
        adminLogRepository.save(new AdminLog(adminUser, "TEST_ACTION", "USER", studentBuyer.getId(), "Manual test log"));

        mockMvc.perform(get("/admin/logs"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/logs/list"))
                .andExpect(model().attributeExists("logs"));
    }
}

