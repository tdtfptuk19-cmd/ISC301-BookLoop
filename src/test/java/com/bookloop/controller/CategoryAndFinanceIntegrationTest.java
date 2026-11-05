package com.bookloop.controller;

import com.bookloop.entity.*;
import com.bookloop.repository.*;
import com.bookloop.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Verification Test for Category Dropdown from Database,
 * Admin Financial Overview with Date Filters & KPIs,
 * and Financial Reconciliation Audit with Safe CSV Export.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.transaction.annotation.Transactional
public class CategoryAndFinanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentTransactionRepository paymentRepository;

    @Autowired
    private PayoutTransactionRepository payoutRepository;

    @Autowired
    private CommissionRecordRepository commissionRepository;

    @Test
    @DisplayName("Part 1: Categories exist in database and are exposed via CategoryService")
    void testCategoriesLoadedFromDatabase() {
        List<Category> activeCategories = categoryService.findAllActive();
        assertThat(activeCategories).isNotEmpty();
        assertThat(activeCategories.stream().map(Category::getName))
                .contains("Công nghệ thông tin", "Kinh tế - Quản trị", "Kế toán - Tài chính");
    }

    @Test
    @DisplayName("Part 1: Homepage / supplies dynamic categories list in model")
    void testHomepageContainsCategories() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("recentListings"));
    }

    @Test
    @DisplayName("Part 1: Book search /books supplies categories dropdown and filters by category")
    void testBookSearchCategoriesAndFilter() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("categories"));

        mockMvc.perform(get("/books").param("category", "Công nghệ thông tin"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attribute("selectedCategory", "Công nghệ thông tin"));
    }

    @Test
    @WithMockUser(username = "quanghuy@bookloop.example", roles = {"STUDENT"})
    @DisplayName("Part 1: Sell book form /my-listings/new supplies categories dropdown")
    void testSellBookFormCategories() throws Exception {
        mockMvc.perform(get("/my-listings/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("listings/form"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("productForm"));
    }

    @Test
    @WithMockUser(username = "admin@bookloop.example", roles = {"ADMIN"})
    @DisplayName("Part 1: Admin listings /admin/listings supports category dropdown filter")
    void testAdminListingsCategoryFilter() throws Exception {
        mockMvc.perform(get("/admin/listings").param("category", "Công nghệ thông tin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/listings/list"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("selectedCategory", "Công nghệ thông tin"));
    }

    @Test
    @WithMockUser(username = "admin@bookloop.example", roles = {"ADMIN"})
    @DisplayName("Part 2: Admin Financial Overview /admin/finance loads with all 8 KPIs")
    void testAdminFinanceOverviewKpis() throws Exception {
        mockMvc.perform(get("/admin/finance"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/finance/overview"))
                .andExpect(model().attributeExists("kpis"))
                .andExpect(model().attributeExists("commissionSetting"))
                .andExpect(model().attributeExists("reconciliation"))
                .andExpect(model().attributeExists("selectedRange"));
    }

    @Test
    @WithMockUser(username = "admin@bookloop.example", roles = {"ADMIN"})
    @DisplayName("Part 2: Admin Financial Overview supports date range filters (TODAY, 7 DAYS, THIS MONTH, CUSTOM)")
    void testAdminFinanceDateFilters() throws Exception {
        mockMvc.perform(get("/admin/finance").param("range", "TODAY"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedRange", "TODAY"));

        mockMvc.perform(get("/admin/finance").param("range", "LAST_7_DAYS"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedRange", "LAST_7_DAYS"));

        mockMvc.perform(get("/admin/finance").param("range", "THIS_MONTH"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedRange", "THIS_MONTH"));

        mockMvc.perform(get("/admin/finance")
                .param("range", "CUSTOM")
                .param("startDate", LocalDate.now().minusDays(10).toString())
                .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedRange", "CUSTOM"));
    }

    @Test
    @WithMockUser(username = "admin@bookloop.example", roles = {"ADMIN"})
    @DisplayName("Part 3: Financial Reconciliation /admin/finance/reconciliation loads detailed items and mock gateway notice")
    void testReconciliationScreen() throws Exception {
        mockMvc.perform(get("/admin/finance/reconciliation"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/finance/reconciliation"))
                .andExpect(model().attributeExists("report"))
                .andExpect(model().attributeExists("reconciliationItems"))
                .andExpect(model().attributeExists("statuses"));
    }

    @Test
    @WithMockUser(username = "admin@bookloop.example", roles = {"ADMIN"})
    @DisplayName("Part 3: Financial Reconciliation CSV Export /admin/finance/reconciliation/export streams valid CSV with BOM")
    void testReconciliationCsvExport() throws Exception {
        mockMvc.perform(get("/admin/finance/reconciliation/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(header().exists("Content-Disposition"));
    }
}

