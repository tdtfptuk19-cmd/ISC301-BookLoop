package com.bookloop.controller;

import com.bookloop.config.DemoAccountSeeder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HomepageRedesignIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DemoAccountSeeder demoAccountSeeder;

    @Test
    @DisplayName("Verify Homepage renders correctly with Hero Section, Headline, Search, and Navigation")
    void testHomepageRendersSuccessfully() throws Exception {
        demoAccountSeeder.run();

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("recentListings", "promotedListings", "totalListings", "institutions"))
                // 1. Navigation items
                .andExpect(content().string(containsString("BookLoop")))
                .andExpect(content().string(containsString("Chợ giáo trình sinh viên Hòa Lạc")))
                .andExpect(content().string(containsString("Trang chủ")))
                .andExpect(content().string(containsString("Tìm sách")))
                .andExpect(content().string(containsString("Tìm giáo trình")))
                .andExpect(content().string(containsString("Đăng bán sách")))
                // 2. Hero Section & Headline
                .andExpect(content().string(containsString("hero-title")))
                .andExpect(content().string(containsString("Mua bán giáo trình &amp; sách cũ sinh viên")))
                .andExpect(content().string(containsString("Tiết kiệm đến 70% chi phí học tập")))
                .andExpect(content().string(containsString("Dành riêng cho sinh viên Đại học khu vực Hòa Lạc")))
                // 3. Search Box & Quick Tags
                .andExpect(content().string(containsString("hero-search-box")))
                .andExpect(content().string(containsString("Mục lục giáo trình")))
                // 4. Stats bar
                .andExpect(content().string(containsString("bl-stats")))
                .andExpect(content().string(containsString("Sách đang rao bán")))
                .andExpect(content().string(containsString("Sinh viên trao đổi")))
                .andExpect(content().string(containsString("Tiết kiệm trung bình")))
                // 5. Sections below hero
                .andExpect(content().string(containsString("Sách mới đăng bán")))
                .andExpect(content().string(containsString("Khám phá giáo trình theo trường Đại học tại Hòa Lạc")))
                .andExpect(content().string(containsString("Quy trình giao dịch an toàn trên BookLoop")))
                .andExpect(content().string(containsString("community-cta-banner")));
    }

    @Test
    @DisplayName("Verify Authenticated Student sees Cart, My Listings, and User Dropdown on Homepage")
    @WithMockUser(username = "quanghuy@bookloop.example", roles = {"STUDENT"})
    void testHomepageAuthenticatedStudentView() throws Exception {
        demoAccountSeeder.run();

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("btn-cart-nav")))
                .andExpect(content().string(containsString("Tin của tôi")))
                .andExpect(content().string(containsString("quanghuy@bookloop.example")));
    }

    @Test
    @DisplayName("Verify Static Hero Image is present in file system")
    void testHeroBackgroundImageFileExists() {
        java.io.File heroImage = new java.io.File("src/main/resources/static/images/hero-study-bg.jpg");
        org.assertj.core.api.Assertions.assertThat(heroImage).exists();
        org.assertj.core.api.Assertions.assertThat(heroImage.length()).isGreaterThan(50000L);
    }
}

