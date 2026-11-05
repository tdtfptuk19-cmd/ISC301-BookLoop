package com.bookloop.controller;

import com.bookloop.dto.CheckoutForm;
import com.bookloop.dto.RegistrationForm;
import com.bookloop.entity.*;
import com.bookloop.repository.*;
import com.bookloop.service.CartService;
import com.bookloop.service.OrderService;
import com.bookloop.service.ReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive End-to-End Integration and Validation Test Suite covering
 * all 10 phases of BookLoop system testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookloopFullE2EIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private com.bookloop.repository.ReportRepository reportRepository;

    @Autowired
    private com.bookloop.repository.AdminLogRepository adminLogRepository;

    @Autowired
    private com.bookloop.repository.PromotionPaymentRepository promotionPaymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewService reviewService;

    private User testBuyer;
    private User testSeller;
    private User testStranger;
    private User testAdmin;

    private Product bookA;
    private Product bookB;
    private Product bookC;
    private Product bookD;

    @BeforeEach
    void setupTestData() {
        cleanDatabase();

        // 1. Prepare Dedicated Test Accounts
        testBuyer = new User();
        testBuyer.setFullName("Test Buyer");
        testBuyer.setEmail("buyer.test@university.edu");
        testBuyer.setPassword(passwordEncoder.encode("buyerpass123"));
        testBuyer.setRole(Role.STUDENT);
        testBuyer.setCampus("Main Campus");
        testBuyer.setPhone("+1-555-0111");
        testBuyer = userRepository.save(testBuyer);

        testSeller = new User();
        testSeller.setFullName("Test Seller");
        testSeller.setEmail("seller.test@university.edu");
        testSeller.setPassword(passwordEncoder.encode("sellerpass123"));
        testSeller.setRole(Role.STUDENT);
        testSeller.setCampus("North Campus");
        testSeller.setPhone("+1-555-0222");
        testSeller = userRepository.save(testSeller);

        testStranger = new User();
        testStranger.setFullName("Test Stranger");
        testStranger.setEmail("stranger.test@university.edu");
        testStranger.setPassword(passwordEncoder.encode("strangerpass123"));
        testStranger.setRole(Role.STUDENT);
        testStranger.setCampus("West Campus");
        testStranger.setPhone("+1-555-0333");
        testStranger = userRepository.save(testStranger);

        testAdmin = new User();
        testAdmin.setFullName("Test Admin");
        testAdmin.setEmail("admin.test@university.edu");
        testAdmin.setPassword(passwordEncoder.encode("adminpass123"));
        testAdmin.setRole(Role.ADMIN);
        testAdmin.setCampus("Admin Building");
        testAdmin.setPhone("+1-555-0999");
        testAdmin = userRepository.save(testAdmin);

        // 2. Prepare Test Books
        // Book A: Active listing, available for purchase
        bookA = new Product();
        bookA.setTitle("Book A - Software Engineering");
        bookA.setAuthor("Ian Sommerville");
        bookA.setPrice(new BigDecimal("40.00"));
        bookA.setCondition(Condition.LIKE_NEW);
        bookA.setCategory("Computer Science");
        bookA.setCampus("North Campus Library");
        bookA.setStatus(ListingStatus.AVAILABLE);
        bookA.setSeller(testSeller);
        bookA = productRepository.save(bookA);

        // Book B: Active listing, available for purchase
        bookB = new Product();
        bookB.setTitle("Book B - Discrete Mathematics");
        bookB.setAuthor("Kenneth Rosen");
        bookB.setPrice(new BigDecimal("50.00"));
        bookB.setCondition(Condition.GOOD);
        bookB.setCategory("Mathematics");
        bookB.setCampus("North Campus Student Center");
        bookB.setStatus(ListingStatus.AVAILABLE);
        bookB.setSeller(testSeller);
        bookB = productRepository.save(bookB);

        // Book C: Inactive or unavailable listing (SOLD)
        bookC = new Product();
        bookC.setTitle("Book C - Chemistry 101");
        bookC.setAuthor("Raymond Chang");
        bookC.setPrice(new BigDecimal("30.00"));
        bookC.setCondition(Condition.ACCEPTABLE);
        bookC.setCategory("Chemistry");
        bookC.setCampus("North Campus Science Hall");
        bookC.setStatus(ListingStatus.SOLD);
        bookC.setSeller(testSeller);
        bookC = productRepository.save(bookC);

        // Book D: Listing owned by the buyer account
        bookD = new Product();
        bookD.setTitle("Book D - Buyer's Own Novel");
        bookD.setAuthor("F. Scott Fitzgerald");
        bookD.setPrice(new BigDecimal("15.00"));
        bookD.setCondition(Condition.NEW);
        bookD.setCategory("Literature");
        bookD.setCampus("Main Campus");
        bookD.setStatus(ListingStatus.AVAILABLE);
        bookD.setSeller(testBuyer);
        bookD = productRepository.save(bookD);
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
    // PHASE 1 — ENVIRONMENT AND STARTUP
    // =========================================================================

    @Test
    @DisplayName("Phase 1: Environment and Application Startup Verification")
    void testPhase1_EnvironmentAndStartup() {
        assertNotNull(applicationContext, "ApplicationContext must be loaded");
        assertNotNull(dataSource, "DataSource must be configured and accessible");
        assertNotNull(userRepository, "UserRepository bean must be active");
        assertNotNull(productRepository, "ProductRepository bean must be active");
        assertNotNull(cartRepository, "CartRepository bean must be active");
        assertNotNull(orderRepository, "OrderRepository bean must be active");
        assertNotNull(reviewRepository, "ReviewRepository bean must be active");
        assertNotNull(cartService, "CartService bean must be active");
        assertNotNull(orderService, "OrderService bean must be active");
        assertNotNull(reviewService, "ReviewService bean must be active");
    }

    // =========================================================================
    // PHASE 2 — TEST DATA PREPARATION
    // =========================================================================

    @Test
    @DisplayName("Phase 2: Test Data Records and Role Verification")
    void testPhase2_TestDataIntegrity() {
        assertEquals(4, userRepository.count());
        assertEquals(4, productRepository.count());

        assertEquals(Role.STUDENT, testBuyer.getRole());
        assertEquals(Role.STUDENT, testSeller.getRole());
        assertEquals(Role.ADMIN, testAdmin.getRole());

        assertEquals(ListingStatus.AVAILABLE, bookA.getStatus());
        assertEquals(ListingStatus.AVAILABLE, bookB.getStatus());
        assertEquals(ListingStatus.SOLD, bookC.getStatus());
        assertEquals(ListingStatus.AVAILABLE, bookD.getStatus());
        assertEquals(testBuyer.getId(), bookD.getSeller().getId());
    }

    // =========================================================================
    // PHASE 3 — AUTHENTICATION AND AUTHORIZATION TESTING
    // =========================================================================

    @Test
    @DisplayName("Phase 3: Registration with valid information")
    void testPhase3_Registration_Valid() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "New Student")
                        .param("email", "newstudent@university.edu")
                        .param("password", "newpass123")
                        .param("confirmPassword", "newpass123")
                        .param("university", "State University")
                        .param("campus", "Main Campus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        User created = userRepository.findByEmailIgnoreCase("newstudent@university.edu").orElse(null);
        assertNotNull(created);
        assertEquals("New Student", created.getFullName());
        assertTrue(passwordEncoder.matches("newpass123", created.getPassword()));
    }

    @Test
    @DisplayName("Phase 3: Registration with duplicate email rejected")
    void testPhase3_Registration_DuplicateEmail() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Duplicate User")
                        .param("email", testBuyer.getEmail())
                        .param("password", "pass12345")
                        .param("confirmPassword", "pass12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("Phase 3: Login valid credentials, invalid credentials, and logout")
    void testPhase3_LoginAndLogout() throws Exception {
        // Valid login
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", testBuyer.getEmail())
                        .param("password", "buyerpass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Invalid login
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", testBuyer.getEmail())
                        .param("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));

        // Logout
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Phase 3: Protected pages redirect without authentication")
    void testPhase3_ProtectedPagesRedirect() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/orders/my-purchases"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/orders/my-sales"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Phase 3: Authorization — Buyer cannot modify another seller's listing")
    void testPhase3_Authorization_CannotModifyOthersListing() throws Exception {
        mockMvc.perform(get("/my-listings/" + bookA.getId() + "/edit")
                        .with(user(testBuyer.getEmail())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-listings"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Phase 3: Authorization — Stranger cannot view another user's order")
    void testPhase3_Authorization_StrangerCannotViewOrder() throws Exception {
        // Create an order between buyer and seller
        cartService.addToCart(testBuyer, bookA.getId());
        CheckoutForm form = new CheckoutForm();
        form.setPaymentMethod(PaymentMethod.IN_PERSON_MEETUP);
        Order order = orderService.checkout(testBuyer, form);

        // Stranger attempts to view order detail
        mockMvc.perform(get("/orders/" + order.getId())
                        .with(user(testStranger.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));
    }

    // =========================================================================
    // PHASE 4 — COMPLETE BUYER WORKFLOW
    // =========================================================================

    @Test
    @DisplayName("Phase 4: Step 1 — Browse, search, and detail page")
    void testPhase4_Step1_BrowseAndDetail() throws Exception {
        // Browse
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("products"));

        // Search by keyword
        mockMvc.perform(get("/books").param("keyword", "Software"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("products"));

        // Book Detail
        mockMvc.perform(get("/books/" + bookA.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("books/detail"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    @DisplayName("Phase 4: Step 2 — Shopping Cart operations and constraint validation")
    void testPhase4_Step2_ShoppingCartWorkflow() throws Exception {
        // Add Book A
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .with(user(testBuyer.getEmail()))
                        .param("productId", bookA.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        // Add Book B
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .with(user(testBuyer.getEmail()))
                        .param("productId", bookB.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        // Verify cart has 2 books and total is $90.00
        mockMvc.perform(get("/cart").with(user(testBuyer.getEmail())))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/view"))
                .andExpect(model().attribute("totalAmount", new BigDecimal("90.00")));

        // Remove Book B
        mockMvc.perform(post("/cart/remove")
                        .with(csrf())
                        .with(user(testBuyer.getEmail()))
                        .param("productId", bookB.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        // Verify cart total updated to $40.00
        mockMvc.perform(get("/cart").with(user(testBuyer.getEmail())))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalAmount", new BigDecimal("40.00")));

        // Attempt to add buyer's own Book D -> Rejected
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .with(user(testBuyer.getEmail()))
                        .param("productId", bookD.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + bookD.getId()))
                .andExpect(flash().attributeExists("errorMessage"));

        // Attempt to add unavailable Book C -> Rejected
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .with(user(testBuyer.getEmail()))
                        .param("productId", bookC.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + bookC.getId()))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Phase 4: Step 3 — Checkout with Meetup")
    void testPhase4_Step3_CheckoutMeetup() throws Exception {
        // Prepare cart with Book A
        cartService.addToCart(testBuyer, bookA.getId());

        // Perform checkout
        mockMvc.perform(post("/checkout")
                        .with(csrf())
                        .with(user(testBuyer.getEmail()))
                        .param("paymentMethod", "IN_PERSON_MEETUP")
                        .param("meetupNote", "Meet at Library Ground Floor at 3 PM")
                        .param("contactPhone", "+1-555-7777"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*"));

        // Verify database state
        List<Order> buyerOrders = orderRepository.findByBuyerIdOrderByOrderDateDesc(testBuyer.getId());
        assertEquals(1, buyerOrders.size());
        Order order = buyerOrders.get(0);

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(PaymentMethod.IN_PERSON_MEETUP, order.getPaymentMethod());
        assertEquals(new BigDecimal("40.00"), order.getTotalAmount());
        assertEquals(testBuyer.getId(), order.getBuyer().getId());
        assertEquals("Meet at Library Ground Floor at 3 PM", order.getMeetupNote());
        assertEquals("+1-555-7777", order.getContactPhone());

        // Verify order items and historical price
        assertEquals(1, order.getItems().size());
        OrderItem item = order.getItems().get(0);
        assertEquals(bookA.getTitle(), item.getBookTitle());
        assertEquals(new BigDecimal("40.00"), item.getPriceAtPurchase());
        assertEquals(testSeller.getId(), item.getSeller().getId());

        // Verify Book A is now marked SOLD
        Product freshA = productRepository.findById(bookA.getId()).orElseThrow();
        assertEquals(ListingStatus.SOLD, freshA.getStatus());

        // Verify cart is cleared
        Cart buyerCart = cartService.getCart(testBuyer);
        assertEquals(0, buyerCart.getItemCount());
    }

    @Test
    @DisplayName("Phase 4: Step 4 — Checkout with Bank Transfer")
    void testPhase4_Step4_CheckoutBankTransfer() throws Exception {
        cartService.addToCart(testBuyer, bookB.getId());

        mockMvc.perform(post("/checkout")
                        .with(csrf())
                        .with(user(testBuyer.getEmail()))
                        .param("paymentMethod", "BANK_TRANSFER")
                        .param("meetupNote", "Direct bank transfer to student account")
                        .param("contactPhone", "+1-555-8888"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*"));

        List<Order> buyerOrders = orderRepository.findByBuyerIdOrderByOrderDateDesc(testBuyer.getId());
        assertEquals(1, buyerOrders.size());
        Order order = buyerOrders.get(0);

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(PaymentMethod.BANK_TRANSFER, order.getPaymentMethod());
        assertEquals(new BigDecimal("50.00"), order.getTotalAmount());

        Product freshB = productRepository.findById(bookB.getId()).orElseThrow();
        assertEquals(ListingStatus.SOLD, freshB.getStatus());
    }

    // =========================================================================
    // PHASE 5 — SELLER WORKFLOW
    // =========================================================================

    @Test
    @DisplayName("Phase 5: Seller orders, details, and order confirmation")
    void testPhase5_SellerWorkflow() throws Exception {
        cartService.addToCart(testBuyer, bookA.getId());
        CheckoutForm form = new CheckoutForm();
        form.setPaymentMethod(PaymentMethod.IN_PERSON_MEETUP);
        Order order = orderService.checkout(testBuyer, form);

        // 1. Seller views My Sales
        mockMvc.perform(get("/orders/my-sales").with(user(testSeller.getEmail())))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/sales"))
                .andExpect(model().attributeExists("orders"));

        // 2. Seller views order details
        mockMvc.perform(get("/orders/" + order.getId()).with(user(testSeller.getEmail())))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/detail"))
                .andExpect(model().attribute("isSeller", true))
                .andExpect(model().attribute("isBuyer", false));

        // 3. Seller confirms PENDING order
        mockMvc.perform(post("/orders/" + order.getId() + "/confirm")
                        .with(csrf())
                        .with(user(testSeller.getEmail())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/" + order.getId()));

        Order confirmed = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, confirmed.getStatus());

        // 4. Attempt to confirm again -> Rejection handled gracefully
        mockMvc.perform(post("/orders/" + order.getId() + "/confirm")
                        .with(csrf())
                        .with(user(testSeller.getEmail())))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));

        // 5. Attempt by another seller/user to confirm -> Rejection handled
        mockMvc.perform(post("/orders/" + order.getId() + "/confirm")
                        .with(csrf())
                        .with(user(testStranger.getEmail())))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // =========================================================================
    // PHASE 6 — ORDER LIFECYCLE TESTING
    // =========================================================================

    @Test
    @DisplayName("Phase 6: Full valid lifecycle PENDING -> CONFIRMED -> COMPLETED")
    void testPhase6_ValidLifecycle() throws Exception {
        cartService.addToCart(testBuyer, bookA.getId());
        Order order = orderService.checkout(testBuyer, new CheckoutForm());
        assertEquals(OrderStatus.PENDING, order.getStatus());

        // Confirm
        orderService.confirmOrder(order.getId(), testSeller);
        Order confirmed = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, confirmed.getStatus());

        // Complete
        orderService.completeOrder(order.getId(), testSeller);
        Order completed = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.COMPLETED, completed.getStatus());
    }

    @Test
    @DisplayName("Phase 6: Cancellation from PENDING restores book availability")
    void testPhase6_CancelFromPending_RestoresBook() throws Exception {
        cartService.addToCart(testBuyer, bookA.getId());
        Order order = orderService.checkout(testBuyer, new CheckoutForm());
        assertEquals(ListingStatus.SOLD, productRepository.findById(bookA.getId()).orElseThrow().getStatus());

        // Buyer cancels
        orderService.cancelOrder(order.getId(), testBuyer);

        Order cancelled = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());

        Product restored = productRepository.findById(bookA.getId()).orElseThrow();
        assertEquals(ListingStatus.AVAILABLE, restored.getStatus());
    }

    @Test
    @DisplayName("Phase 6: Cancellation from CONFIRMED restores book availability")
    void testPhase6_CancelFromConfirmed_RestoresBook() throws Exception {
        cartService.addToCart(testBuyer, bookA.getId());
        Order order = orderService.checkout(testBuyer, new CheckoutForm());
        orderService.confirmOrder(order.getId(), testSeller);

        // Seller cancels
        orderService.cancelOrder(order.getId(), testSeller);

        Order cancelled = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());

        Product restored = productRepository.findById(bookA.getId()).orElseThrow();
        assertEquals(ListingStatus.AVAILABLE, restored.getStatus());
    }

    @Test
    @DisplayName("Phase 6: Invalid transitions and unauthorized actions are strictly rejected")
    void testPhase6_InvalidTransitions() {
        cartService.addToCart(testBuyer, bookA.getId());
        Order order = orderService.checkout(testBuyer, new CheckoutForm());

        // Direct PENDING -> COMPLETED rejected
        assertThrows(IllegalStateException.class, () -> orderService.completeOrder(order.getId(), testSeller));

        // Buyer cannot confirm order
        assertThrows(RuntimeException.class, () -> orderService.confirmOrder(order.getId(), testBuyer));

        // Complete order then try to cancel
        orderService.confirmOrder(order.getId(), testSeller);
        orderService.completeOrder(order.getId(), testSeller);

        // COMPLETED -> CANCELLED rejected
        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(order.getId(), testBuyer));
        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(order.getId(), testSeller));
    }

    // =========================================================================
    // PHASE 7 — REVIEWS AND RATINGS
    // =========================================================================

    @Test
    @DisplayName("Phase 7: Review submission on completed order and profile average calculation")
    void testPhase7_ReviewSubmissionAndProfileDisplay() throws Exception {
        // Complete an order
        cartService.addToCart(testBuyer, bookA.getId());
        Order order = orderService.checkout(testBuyer, new CheckoutForm());
        orderService.confirmOrder(order.getId(), testSeller);
        orderService.completeOrder(order.getId(), testSeller);

        // Buyer submits 5-star review
        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .with(user(testBuyer.getEmail()))
                        .param("orderId", order.getId().toString())
                        .param("sellerId", testSeller.getId().toString())
                        .param("rating", "5")
                        .param("comment", "Prompt meetup and textbook in great condition!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/" + order.getId()))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify review in database
        List<Review> sellerReviews = reviewRepository.findBySellerIdOrderByCreatedAtDesc(testSeller.getId());
        assertEquals(1, sellerReviews.size());
        assertEquals(5, sellerReviews.get(0).getRating());
        assertEquals("Prompt meetup and textbook in great condition!", sellerReviews.get(0).getComment());

        // Verify seller public profile displays review and 5.0 average
        mockMvc.perform(get("/profile/seller/" + testSeller.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/seller"))
                .andExpect(model().attribute("avgRating", 5.0))
                .andExpect(model().attribute("reviewCount", 1L))
                .andExpect(model().attributeExists("reviews"));

        // Attempt duplicate review -> Rejected
        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .with(user(testBuyer.getEmail()))
                        .param("orderId", order.getId().toString())
                        .param("sellerId", testSeller.getId().toString())
                        .param("rating", "4")
                        .param("comment", "Duplicate review attempt"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));

        // Attempt review with invalid rating (6 stars) -> Rejected
        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .with(user(testBuyer.getEmail()))
                        .param("orderId", order.getId().toString())
                        .param("sellerId", testSeller.getId().toString())
                        .param("rating", "6")
                        .param("comment", "Invalid star rating"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Phase 7: Reviewing incomplete order or by non-buyer is rejected")
    void testPhase7_ReviewIncompleteOrder_Rejected() {
        cartService.addToCart(testBuyer, bookA.getId());
        Order order = orderService.checkout(testBuyer, new CheckoutForm()); // Status: PENDING

        // Incomplete order review attempt rejected
        assertThrows(IllegalStateException.class, () ->
                reviewService.submitReview(order.getId(), testSeller.getId(), 5, "Too early", testBuyer));

        // Non-buyer review attempt rejected
        orderService.confirmOrder(order.getId(), testSeller);
        orderService.completeOrder(order.getId(), testSeller);
        assertThrows(RuntimeException.class, () ->
                reviewService.submitReview(order.getId(), testSeller.getId(), 5, "Imposter", testStranger));
    }

    // =========================================================================
    // PHASE 8 — ADMIN WORKFLOW AUDIT
    // =========================================================================

    @Test
    @DisplayName("Phase 8: Admin Role existence and access audit")
    void testPhase8_AdminRoleAudit() {
        assertEquals(Role.ADMIN, testAdmin.getRole());
        assertNotNull(Role.valueOf("ADMIN"));
        // Confirm Admin functionality status: Role is in entity model, dedicated admin portal is future phase
    }

    // =========================================================================
    // PHASE 9 — DATABASE INTEGRITY TESTING
    // =========================================================================

    @Test
    @DisplayName("Phase 9: Historical snapshot integrity — price and title immutability")
    void testPhase9_HistoricalPriceSnapshotIntegrity() {
        cartService.addToCart(testBuyer, bookA.getId());
        Order order = orderService.checkout(testBuyer, new CheckoutForm());

        OrderItem item = order.getItems().get(0);
        assertEquals(new BigDecimal("40.00"), item.getPriceAtPurchase());
        assertEquals("Book A - Software Engineering", item.getBookTitle());

        // Seller subsequently changes listing price and title in catalog
        bookA.setPrice(new BigDecimal("99.99"));
        bookA.setTitle("Altered Title - Software Engineering");
        productRepository.save(bookA);

        // Historical order item must remain completely untouched
        OrderItem refreshedItem = orderItemRepository.findById(item.getId()).orElseThrow();
        assertEquals(new BigDecimal("40.00"), refreshedItem.getPriceAtPurchase());
        assertEquals("Book A - Software Engineering", refreshedItem.getBookTitle());
    }

    @Test
    @DisplayName("Phase 9: Cascade deletion and orphan removal")
    void testPhase9_CascadeDeletion() {
        cartService.addToCart(testBuyer, bookA.getId());
        Cart cart = cartRepository.findByUserIdWithItems(testBuyer.getId()).orElseThrow();
        Long cartId = cart.getId();

        assertEquals(1, cartItemRepository.findAll().size());

        // Clear cart cascades and deletes cart items
        cartService.clearCart(testBuyer);
        assertEquals(0, cartItemRepository.findAll().size());
    }

    // =========================================================================
    // PHASE 10 — SECURITY AND EDGE-CASE TESTING
    // =========================================================================

    @Test
    @DisplayName("Phase 10: Price manipulation in request is completely ignored by server")
    void testPhase10_PriceManipulationIgnored() throws Exception {
        cartService.addToCart(testBuyer, bookA.getId()); // Book A price is $40.00

        // Malicious client posts modified price in form data
        mockMvc.perform(post("/checkout")
                        .with(csrf())
                        .with(user(testBuyer.getEmail()))
                        .param("totalAmount", "0.01")
                        .param("paymentMethod", "IN_PERSON_MEETUP"))
                .andExpect(status().is3xxRedirection());

        Order created = orderRepository.findByBuyerIdOrderByOrderDateDesc(testBuyer.getId()).get(0);
        // Server MUST use its own calculated total ($40.00), NEVER trusting the client
        assertEquals(new BigDecimal("40.00"), created.getTotalAmount());
    }

    @Test
    @DisplayName("Phase 10: POST request without CSRF token is rejected with 403 Forbidden")
    void testPhase10_CsrfProtectionEnforced() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .with(user(testBuyer.getEmail()))
                        .param("productId", bookA.getId().toString()))
                .andExpect(status().isForbidden());
    }
}
