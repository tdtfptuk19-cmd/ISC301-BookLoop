package com.bookloop.service;

import com.bookloop.entity.*;
import com.bookloop.repository.CartItemRepository;
import com.bookloop.repository.CartRepository;
import com.bookloop.repository.ProductRepository;
import com.bookloop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartServiceTest {

    @Autowired
    private CartService cartService;

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
    private Product book;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        buyer = createUser("Student Buyer", "buyer@university.edu");
        seller = createUser("Student Seller", "seller@university.edu");

        book = createProduct("Calculus Early Transcendentals", new BigDecimal("45.00"), seller, ListingStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Add a valid book to cart successfully")
    void addToCart_success() {
        cartService.addToCart(buyer, book.getId());

        Cart cart = cartService.getCart(buyer);
        assertEquals(1, cart.getItemCount());
        assertEquals(new BigDecimal("45.00"), cart.calculateTotal());
    }

    @Test
    @DisplayName("Add the same book more than once preserves single copy for second-hand listing")
    void addToCart_duplicateBook() {
        cartService.addToCart(buyer, book.getId());
        cartService.addToCart(buyer, book.getId());

        Cart cart = cartService.getCart(buyer);
        assertEquals(1, cart.getItemCount());
        assertEquals(new BigDecimal("45.00"), cart.calculateTotal());
    }

    @Test
    @DisplayName("Remove a book from cart")
    void removeFromCart_success() {
        cartService.addToCart(buyer, book.getId());
        assertEquals(1, cartService.getCart(buyer).getItemCount());

        cartService.removeFromCart(buyer, book.getId());
        assertEquals(0, cartService.getCart(buyer).getItemCount());
    }

    @Test
    @DisplayName("Attempting to add your own listing throws exception")
    void addToCart_ownListing_rejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                cartService.addToCart(seller, book.getId())
        );
        assertTrue(ex.getMessage().contains("own listed book"));
    }

    @Test
    @DisplayName("Attempting to add an unavailable listing throws exception")
    void addToCart_unavailableListing_rejected() {
        book.setStatus(ListingStatus.SOLD);
        productRepository.save(book);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                cartService.addToCart(buyer, book.getId())
        );
        assertTrue(ex.getMessage().contains("cannot be purchased"));
    }

    // --- Helpers ---

    private User createUser(String name, String email) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("secret"));
        user.setRole(Role.STUDENT);
        user.setCampus("Main Campus");
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
