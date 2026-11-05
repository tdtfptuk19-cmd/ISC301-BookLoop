package com.bookloop.service;

import com.bookloop.dto.CheckoutForm;
import com.bookloop.entity.*;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User buyer;
    private User seller;
    private User stranger;
    private Product book;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        buyer = createUser("Alice Buyer", "alice@university.edu");
        seller = createUser("Bob Seller", "bob@university.edu");
        stranger = createUser("Eve Stranger", "eve@university.edu");

        book = createProduct("Calculus Early Transcendentals", new BigDecimal("40.00"), seller, ListingStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Review a completed transaction successfully and calculate seller average rating")
    void submitReview_success() {
        Order order = createCompletedOrder();

        Review review = reviewService.submitReview(
                order.getId(), seller.getId(), 5, "Book was in great condition, smooth meetup!", buyer);

        assertNotNull(review.getId());
        assertEquals(5, review.getRating());
        assertEquals("Book was in great condition, smooth meetup!", review.getComment());
        assertEquals(seller.getId(), review.getSeller().getId());
        assertEquals(buyer.getId(), review.getBuyer().getId());

        Double avg = reviewService.getAverageRatingForSeller(seller.getId());
        assertEquals(5.0, avg);
        assertEquals(1, reviewService.getReviewCountForSeller(seller.getId()));

        List<Review> reviews = reviewService.getReviewsForSeller(seller.getId());
        assertEquals(1, reviews.size());
    }

    @Test
    @DisplayName("Attempting to review an incomplete transaction (PENDING or CONFIRMED) throws exception")
    void submitReview_incompleteOrder_rejected() {
        cartService.addToCart(buyer, book.getId());
        Order order = orderService.checkout(buyer, new CheckoutForm()); // Status: PENDING

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                reviewService.submitReview(order.getId(), seller.getId(), 4, "Too early", buyer));
        assertTrue(ex.getMessage().contains("COMPLETED"));
    }

    @Test
    @DisplayName("Attempting duplicate reviews for same order and seller throws exception")
    void submitReview_duplicate_rejected() {
        Order order = createCompletedOrder();

        reviewService.submitReview(order.getId(), seller.getId(), 5, "Great!", buyer);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                reviewService.submitReview(order.getId(), seller.getId(), 4, "Duplicate", buyer));
        assertTrue(ex.getMessage().contains("already submitted a review"));
    }

    @Test
    @DisplayName("Attempting to review another user's transaction throws exception")
    void submitReview_unauthorizedUser_rejected() {
        Order order = createCompletedOrder();

        assertThrows(UnauthorizedOperationException.class, () ->
                reviewService.submitReview(order.getId(), seller.getId(), 5, "Not my order", stranger));
    }

    @Test
    @DisplayName("Rating outside 1-5 stars is rejected")
    void submitReview_invalidRating_rejected() {
        Order order = createCompletedOrder();

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.submitReview(order.getId(), seller.getId(), 0, "Zero stars", buyer));

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.submitReview(order.getId(), seller.getId(), 6, "Six stars", buyer));
    }

    // --- Helpers ---

    private Order createCompletedOrder() {
        cartService.addToCart(buyer, book.getId());
        Order order = orderService.checkout(buyer, new CheckoutForm());
        orderService.confirmOrder(order.getId(), seller);
        return orderService.completeOrder(order.getId(), seller);
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("secret"));
        user.setRole(Role.STUDENT);
        user.setCampus("Main Campus");
        user.setPhone("+1-555-0100");
        return userRepository.save(user);
    }

    private Product createProduct(String title, BigDecimal price, User productSeller, ListingStatus status) {
        Product p = new Product();
        p.setTitle(title);
        p.setPrice(price);
        p.setCondition(Condition.GOOD);
        p.setCampus("Main Campus Library");
        p.setStatus(status);
        p.setSeller(productSeller);
        return productRepository.save(p);
    }
}
