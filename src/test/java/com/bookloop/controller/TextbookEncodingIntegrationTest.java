package com.bookloop.controller;

import com.bookloop.entity.*;
import com.bookloop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TextbookEncodingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AcademicBookRepository bookRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private AcademicProgramRepository programRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseBookRepository courseBookRepository;

    private AcademicBook accountingBook;

    @BeforeEach
    void setUp() {
        accountingBook = bookRepository.findByIsbn("978-6049221111")
                .orElseGet(() -> bookRepository.save(new AcademicBook(
                        "978-6049221111",
                        "Giáo trình Nguyên lý kế toán (Học viện Tài chính)",
                        "Principles of Accounting",
                        "GS.TS. Ngô Thế Chi, PGS.TS. Trương Thị Thủy",
                        "NXB Tài Chính",
                        "1st",
                        2021,
                        "Tiếng Việt",
                        "Giáo trình chuẩn mực về nguyên lý kế toán và hạch toán kinh tế tại Học viện Tài chính.",
                        "https://example.com/cover.jpg",
                        "Kế toán - Tài chính",
                        "Giáo trình chính"
                )));
    }

    @Test
    @DisplayName("Verify that /textbooks catalog responds in UTF-8 and displays clean Vietnamese without Mojibake")
    void testTextbookCatalogEncodingUtf8() throws Exception {
        MvcResult result = mockMvc.perform(get("/textbooks"))
                .andExpect(status().isOk())
                .andExpect(view().name("textbooks/browse"))
                .andExpect(model().attributeExists("categories"))
                .andReturn();

        result.getResponse().setCharacterEncoding("UTF-8");
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // Ensure category "Kế toán - Tài chính" is present in dropdown/options
        assertThat(content).contains("Kế toán - Tài chính");

        // Ensure no mojibake corruption strings exist in HTML
        assertThat(content).doesNotContain("Káº¿");
        assertThat(content).doesNotContain("toÃ¡n");
        assertThat(content).doesNotContain("TÃ i");
        assertThat(content).doesNotContain("chÃnh");
        assertThat(content).doesNotContain("GiÃ¡o");
        assertThat(content).doesNotContain("trÃ¬nh");
        assertThat(content).doesNotContain("SÃ¡ch");
    }

    @Test
    @DisplayName("Verify filtering textbooks by 'Kế toán - Tài chính' returns proper accounting books")
    void testFilterTextbooksByAccountingFinanceCategory() throws Exception {
        MvcResult result = mockMvc.perform(get("/textbooks")
                        .param("category", "Kế toán - Tài chính"))
                .andExpect(status().isOk())
                .andExpect(view().name("textbooks/browse"))
                .andExpect(model().attribute("selectedCategory", "Kế toán - Tài chính"))
                .andReturn();

        Page<AcademicBook> books = (Page<AcademicBook>) result.getModelAndView().getModel().get("books");
        assertThat(books).isNotEmpty();
        assertThat(books.getContent()).allMatch(b -> "Kế toán - Tài chính".equals(b.getCategory()));
    }

    @Test
    @DisplayName("Verify search with Vietnamese keyword 'Kế toán' filters accurately")
    void testSearchTextbooksWithVietnameseKeyword() throws Exception {
        MvcResult result = mockMvc.perform(get("/textbooks")
                        .param("keyword", "Kế toán"))
                .andExpect(status().isOk())
                .andExpect(view().name("textbooks/browse"))
                .andExpect(model().attribute("keyword", "Kế toán"))
                .andReturn();

        Page<AcademicBook> books = (Page<AcademicBook>) result.getModelAndView().getModel().get("books");
        assertThat(books).isNotEmpty();
        assertThat(books.getContent()).anyMatch(b -> b.getTitle().contains("kế toán") || b.getTitle().contains("Kế toán")
                || (b.getDescription() != null && b.getDescription().contains("kế toán")));
    }
}

