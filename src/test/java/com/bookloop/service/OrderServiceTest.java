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
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User buyer;
    private User seller;
    private User stranger;
    private Product book1;
    private Product book2;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        buyer = createUser("Alice Buyer", "alice@university.edu");
        seller = createUser("Bob Seller", "bob@university.edu");
        stranger = createUser("Charlie Stranger", "charlie@university.edu");

        book1 = createProduct("Algorithms 4th Edition", new BigDecimal("35.00"), seller, ListingStatus.AVAILABLE);
        book2 = createProduct("Operating Systems Concepts", new BigDecimal("45.00"), seller, ListingStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Checkout creates order, snapshots prices, marks books SOLD, and clears cart")
    void checkout_success() {
        cartService.addToCart(buyer, book1.getId());
        cartService.addToCart(buyer, book2.getId());

        CheckoutForm form = new CheckoutForm();
        form.setPaymentMethod(PaymentMethod.IN_PERSON_MEETUP);
        form.setMeetupNote("Meet at library 2pm");
        form.setContactPhone("+1-555-0199");

        Order order = orderService.checkout(buyer, form);

        assertNotNull(order.getId());
        assertNotNull(order.getOrderNumber());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(new BigDecimal("80.00"), order.getTotalAmount());
        assertEquals(2, order.getItems().size());

        // Verify products are now marked SOLD in DB
        Product fresh1 = productRepository.findById(book1.getId()).orElseThrow();
        Product fresh2 = productRepository.findById(book2.getId()).orElseThrow();
        assertEquals(ListingStatus.SOLD, fresh1.getStatus());
        assertEquals(ListingStatus.SOLD, fresh2.getStatus());

        // Verify buyer's cart is empty
        Cart freshCart = cartService.getCart(buyer);
        assertEquals(0, freshCart.getItemCount());
    }

    @Test
    @DisplayName("Attempting to checkout with an empty cart throws exception")
    void checkout_emptyCart_rejected() {
        CheckoutForm form = new CheckoutForm();
        assertThrows(IllegalArgumentException.class, () -> orderService.checkout(buyer, form));
    }

    @Test
    @DisplayName("Attempting to checkout when a book became unavailable is rejected")
    void checkout_unavailableBook_rejected() {
        cartService.addToCart(buyer, book1.getId());

        // Another buyer or seller marks it sold before checkout
        book1.setStatus(ListingStatus.SOLD);
        productRepository.save(book1);

        CheckoutForm form = new CheckoutForm();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.checkout(buyer, form));
        assertTrue(ex.getMessage().contains("no longer available"));
    }

    @Test
    @DisplayName("Buyer can view their purchases and seller can view their sales")
    void findOrders_buyerAndSeller() {
        cartService.addToCart(buyer, book1.getId());
        Order order = orderService.checkout(buyer, new CheckoutForm());

        List<Order> buyerOrders = orderService.findBuyerOrders(buyer.getId());
        assertEquals(1, buyerOrders.size());
        assertEquals(order.getId(), buyerOrders.get(0).getId());

        List<Order> sellerOrders = orderService.findSellerOrders(seller.getId());
        assertEquals(1, sellerOrders.size());
        assertEquals(order.getId(), sellerOrders.get(0).getId());
    }

    @Test
    @DisplayName("Unauthorized user cannot access another user's order")
    void getOrderDetails_unauthorized() {
        cartService.addToCart(buyer, book1.getId());
        Order order = orderService.checkout(buyer, new CheckoutForm());

        assertThrows(UnauthorizedOperationException.class, () ->
                orderService.getOrderDetails(order.getId(), stranger));
    }

    @Test
    @DisplayName("Seller can confirm order; buyer cannot confirm their own order")
    void confirmOrder_workflow() {
        cartService.addToCart(buyer, book1.getId());
        Order order = orderService.checkout(buyer, new CheckoutForm());

        // Buyer cannot confirm
        assertThrows(UnauthorizedOperationException.class, () ->
                orderService.confirmOrder(order.getId(), buyer));

        // Seller confirms
        Order confirmed = orderService.confirmOrder(order.getId(), seller);
        assertEquals(OrderStatus.CONFIRMED, confirmed.getStatus());
    }

    @Test
    @DisplayName("Seller completes confirmed order")
    void completeOrder_workflow() {
        cartService.addToCart(buyer, book1.getId());
        Order order = orderService.checkout(buyer, new CheckoutForm());
        orderService.confirmOrder(order.getId(), seller);

        Order completed = orderService.completeOrder(order.getId(), seller);
        assertEquals(OrderStatus.COMPLETED, completed.getStatus());
    }

    @Test
    @DisplayName("Invalid status transition (e.g. PENDING directly to COMPLETED) is rejected")
    void completeOrder_fromPending_rejected() {
        cartService.addToCart(buyer, book1.getId());
        Order order = orderService.checkout(buyer, new CheckoutForm());

        assertThrows(IllegalStateException.class, () ->
                orderService.completeOrder(order.getId(), seller));
    }

    @Test
    @DisplayName("Cancelled order restores product availability back to AVAILABLE")
    void cancelOrder_restoresAvailability() {
        cartService.addToCart(buyer, book1.getId());
        Order order = orderService.checkout(buyer, new CheckoutForm());

        assertEquals(ListingStatus.SOLD, productRepository.findById(book1.getId()).orElseThrow().getStatus());

        orderService.cancelOrder(order.getId(), buyer);

        Order cancelledOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());

        Product restored = productRepository.findById(book1.getId()).orElseThrow();
        assertEquals(ListingStatus.AVAILABLE, restored.getStatus());
    }

    @Test
    @DisplayName("Completed order cannot be cancelled")
    void cancelOrder_completedOrder_rejected() {
        cartService.addToCart(buyer, book1.getId());
        Order order = orderService.checkout(buyer, new CheckoutForm());
        orderService.confirmOrder(order.getId(), seller);
        orderService.completeOrder(order.getId(), seller);

        assertThrows(IllegalStateException.class, () ->
                orderService.cancelOrder(order.getId(), buyer));
    }

    // --- Helpers ---

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
