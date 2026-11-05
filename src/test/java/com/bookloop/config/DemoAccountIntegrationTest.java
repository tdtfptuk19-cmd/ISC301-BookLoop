package com.bookloop.config;

import com.bookloop.entity.Role;
import com.bookloop.entity.User;
import com.bookloop.repository.ProductRepository;
import com.bookloop.repository.SellerPaymentAccountRepository;
import com.bookloop.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DemoAccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerPaymentAccountRepository sellerPaymentAccountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DemoAccountSeeder demoAccountSeeder;

    @Test
    @DisplayName("Verify all 4 demo accounts are seeded with correct details and password '123456'")
    void testDemoAccountsSeedingAndCredentials() {
        demoAccountSeeder.run();

        // 1. Admin
        Optional<User> adminOpt = userRepository.findByEmailIgnoreCase("admin@bookloop.example");
        assertThat(adminOpt).isPresent();
        User admin = adminOpt.get();
        assertThat(admin.getFullName()).isEqualTo("BookLoop Demo Admin");
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(passwordEncoder.matches("123456", admin.getPassword())).isTrue();

        // 2. Quang Huy (FPT Buyer & Seller)
        Optional<User> huyOpt = userRepository.findByEmailIgnoreCase("quanghuy@bookloop.example");
        assertThat(huyOpt).isPresent();
        User huy = huyOpt.get();
        assertThat(huy.getFullName()).isEqualTo("Quang Huy");
        assertThat(huy.getRole()).isEqualTo(Role.STUDENT);
        assertThat(huy.getUniversity()).contains("FPT");
        assertThat(passwordEncoder.matches("123456", huy.getPassword())).isTrue();
        assertThat(sellerPaymentAccountRepository.findBySellerId(huy.getId())).isPresent();

        // 3. Vũ Tùng (VNU Seller)
        Optional<User> tungOpt = userRepository.findByEmailIgnoreCase("vutung@bookloop.example");
        assertThat(tungOpt).isPresent();
        User tung = tungOpt.get();
        assertThat(tung.getFullName()).isEqualTo("Vũ Tùng");
        assertThat(tung.getRole()).isEqualTo(Role.STUDENT);
        assertThat(tung.getUniversity()).contains("Quốc gia Hà Nội");
        assertThat(passwordEncoder.matches("123456", tung.getPassword())).isTrue();
        assertThat(sellerPaymentAccountRepository.findBySellerId(tung.getId())).isPresent();

        // 4. Đại Thành (AOF Buyer & Seller)
        Optional<User> thanhOpt = userRepository.findByEmailIgnoreCase("daithanh@bookloop.example");
        assertThat(thanhOpt).isPresent();
        User thanh = thanhOpt.get();
        assertThat(thanh.getFullName()).isEqualTo("Đại Thành");
        assertThat(thanh.getRole()).isEqualTo(Role.STUDENT);
        assertThat(thanh.getUniversity()).contains("Tài chính");
        assertThat(passwordEncoder.matches("123456", thanh.getPassword())).isTrue();
        assertThat(sellerPaymentAccountRepository.findBySellerId(thanh.getId())).isPresent();

        // Check products for all 3 student demo accounts
        long huyListingCount = productRepository.findAll().stream()
                .filter(p -> p.getSeller() != null && p.getSeller().getId().equals(huy.getId())).count();
        assertThat(huyListingCount).isGreaterThanOrEqualTo(6);

        long tungListingCount = productRepository.findAll().stream()
                .filter(p -> p.getSeller() != null && p.getSeller().getId().equals(tung.getId())).count();
        assertThat(tungListingCount).isGreaterThanOrEqualTo(6);

        long thanhListingCount = productRepository.findAll().stream()
                .filter(p -> p.getSeller() != null && p.getSeller().getId().equals(thanh.getId())).count();
        assertThat(thanhListingCount).isGreaterThanOrEqualTo(6);
    }

    @Test
    @DisplayName("Verify Spring Security Form Login with Demo Admin Account")
    void testAdminLoginSuccess() throws Exception {
        demoAccountSeeder.run();

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin@bookloop.example")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Verify Spring Security Form Login with Student Account")
    void testStudentLoginSuccess() throws Exception {
        demoAccountSeeder.run();

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "quanghuy@bookloop.example")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Verify RBAC: Admin can access /admin/dashboard, Student is forbidden (403)")
    void testRbacDashboardAccess() throws Exception {
        demoAccountSeeder.run();

        // Admin access
        mockMvc.perform(get("/admin/dashboard")
                        .with(user("admin@bookloop.example").roles("ADMIN")))
                .andExpect(status().isOk());

        // Student access -> 403 Forbidden
        mockMvc.perform(get("/admin/dashboard")
                        .with(user("quanghuy@bookloop.example").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Verify Buyer Quang Huy can view Seller Vũ Tùng's book and add to cart")
    void testBuyerCanAddToCartFromSeller() throws Exception {
        demoAccountSeeder.run();

        User tung = userRepository.findByEmailIgnoreCase("vutung@bookloop.example").orElseThrow();
        var tungProduct = productRepository.findAll().stream()
                .filter(p -> p.getSeller() != null && p.getSeller().getId().equals(tung.getId()))
                .findFirst().orElseThrow();

        // Quang Huy views detail page
        mockMvc.perform(get("/books/" + tungProduct.getId())
                        .with(user("quanghuy@bookloop.example").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isOwner", false))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Thêm vào giỏ hàng")));

        // Quang Huy adds to cart
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .with(user("quanghuy@bookloop.example").roles("STUDENT"))
                        .param("productId", String.valueOf(tungProduct.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    @DisplayName("Verify Seller Vũ Tùng viewing own book cannot add to cart (isOwner = true)")
    void testSellerViewingOwnBookIsOwner() throws Exception {
        demoAccountSeeder.run();

        User tung = userRepository.findByEmailIgnoreCase("vutung@bookloop.example").orElseThrow();
        var tungProduct = productRepository.findAll().stream()
                .filter(p -> p.getSeller() != null && p.getSeller().getId().equals(tung.getId()))
                .findFirst().orElseThrow();

        // Vũ Tùng views his own listing
        mockMvc.perform(get("/books/" + tungProduct.getId())
                        .with(user("vutung@bookloop.example").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isOwner", true))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Đây là tin đăng bán của bạn")));
    }
}
