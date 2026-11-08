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
 * TC-P8-019 / BR-033:
 * Verifies that /buyer/payment/mock-checkout endpoints return HTTP 404 when
 * bookloop.payment.mock-enabled is false (Production default).
 */
@SpringBootTest(properties = "bookloop.payment.mock-enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MockPaymentProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("TC-P8-019: GET /buyer/payment/mock-checkout returns 404 when mock payment is disabled")
    @WithMockUser(username = "buyer@fpt.edu.vn", roles = {"STUDENT"})
    void testDisabledInProd() throws Exception {
        mockMvc.perform(get("/buyer/payment/mock-checkout"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /buyer/payment/mock-checkout/simulate-success returns 404 when mock payment is disabled")
    @WithMockUser(username = "buyer@fpt.edu.vn", roles = {"STUDENT"})
    void testSimulateSuccessDisabledInProd() throws Exception {
        mockMvc.perform(post("/buyer/payment/mock-checkout/simulate-success")
                        .with(csrf())
                        .param("merchantReference", "REF-TEST-001"))
                .andExpect(status().isNotFound());
    }
}
