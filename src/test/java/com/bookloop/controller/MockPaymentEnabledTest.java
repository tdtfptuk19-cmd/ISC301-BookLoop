package com.bookloop.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that /buyer/payment/mock-checkout endpoints remain accessible (not 404)
 * when bookloop.payment.mock-enabled is true (Dev/Test default).
 */
@SpringBootTest(properties = "bookloop.payment.mock-enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MockPaymentEnabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /buyer/payment/mock-checkout is accessible and does not return 404 when mock payment is enabled")
    @WithMockUser(username = "buyer@fpt.edu.vn", roles = {"STUDENT"})
    void testMockCheckoutEnabled() throws Exception {
        mockMvc.perform(get("/buyer/payment/mock-checkout"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("POST /buyer/payment/mock-checkout/simulate-success is accessible and does not return 404 when mock payment is enabled")
    @WithMockUser(username = "buyer@fpt.edu.vn", roles = {"STUDENT"})
    void testSimulateSuccessEnabled() throws Exception {
        mockMvc.perform(post("/buyer/payment/mock-checkout/simulate-success")
                        .with(csrf())
                        .param("merchantReference", "NON-EXISTENT-REF"))
                .andExpect(status().is3xxRedirection());
    }
}
