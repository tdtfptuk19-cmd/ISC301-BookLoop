package com.bookloop.controller;

import com.bookloop.entity.Role;
import com.bookloop.entity.User;
import com.bookloop.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for security rules — verifying that public pages are accessible
 * and protected pages (including Cart, Checkout, and Orders) redirect to login.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // --- Public pages ---

    @Test
    @DisplayName("Homepage is accessible without authentication")
    void homePage_public() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Book listing page is accessible without authentication")
    void bookList_public() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Login page is accessible without authentication")
    void loginPage_public() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Registration page is accessible without authentication")
    void registerPage_public() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public seller profile is accessible without authentication")
    void sellerProfile_public() throws Exception {
        User seller = new User();
        seller.setFullName("Public Seller");
        seller.setEmail("publicseller@example.com");
        seller.setPassword("password123");
        seller.setRole(Role.STUDENT);
        userRepository.save(seller);

        mockMvc.perform(get("/profile/seller/" + seller.getId()))
                .andExpect(status().isOk());
    }

    // --- Protected pages redirect to login ---

    @Test
    @DisplayName("Profile page requires authentication")
    void profilePage_requiresAuth() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("My Listings page requires authentication")
    void myListings_requiresAuth() throws Exception {
        mockMvc.perform(get("/my-listings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Create listing page requires authentication")
    void createListing_requiresAuth() throws Exception {
        mockMvc.perform(get("/my-listings/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Edit listing page requires authentication")
    void editListing_requiresAuth() throws Exception {
        mockMvc.perform(get("/my-listings/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Shopping Cart page requires authentication")
    void cartPage_requiresAuth() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Checkout page requires authentication")
    void checkoutPage_requiresAuth() throws Exception {
        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("My Purchases page requires authentication")
    void myPurchasesPage_requiresAuth() throws Exception {
        mockMvc.perform(get("/orders/my-purchases"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("My Sales page requires authentication")
    void mySalesPage_requiresAuth() throws Exception {
        mockMvc.perform(get("/orders/my-sales"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
