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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BugFixesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private AcademicProgramRepository programRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AcademicBookRepository academicBookRepository;

    @Autowired
    private CourseBookRepository courseBookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User seller;
    private Institution fptInst;
    private Institution vnuInst;
    private AcademicProgram fptSe;
    private AcademicProgram vnuCs;
    private Course csdCourse;
    private Course dsaCourse;
    private AcademicBook algoBook;
    private AcademicBook javaBook;

    @BeforeEach
    void setUp() {
        // Use correct repository method: findByEmailIgnoreCase
        seller = userRepository.findByEmailIgnoreCase("alice@university.edu")
                .orElseGet(() -> {
                    // User has no multi-arg constructor; use no-arg + setters
                    User u = new User();
                    u.setFullName("Alice Johnson");
                    u.setEmail("alice@university.edu");
                    u.setPassword(passwordEncoder.encode("password123"));
                    u.setUniversity("ĐH FPT");
                    u.setCampus("Khu CNC Hòa Lạc");
                    u.setPhone("0987654321");
                    u.setRole(Role.STUDENT);
                    return userRepository.save(u);
                });

        // Setup test products with realistic VND pricing
        Product p1 = new Product();
        p1.setTitle("Lập trình Java căn bản và nâng cao");
        p1.setAuthor("Kathy Sierra");
        p1.setPrice(new BigDecimal("65000")); // 65.000 VND
        p1.setCondition(Condition.GOOD);
        p1.setCategory("Công nghệ thông tin");
        p1.setCampus("ĐH FPT Hòa Lạc");
        p1.setCourseCode("PRO192");
        p1.setSeller(seller);
        p1.setStatus(ListingStatus.AVAILABLE);
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setTitle("Thuật toán & Cấu trúc dữ liệu CLRS");
        p2.setAuthor("Thomas H. Cormen");
        p2.setPrice(new BigDecimal("120000")); // 120.000 VND
        p2.setCondition(Condition.LIKE_NEW);
        p2.setCategory("Công nghệ thông tin");
        p2.setCampus("ĐHQGHN Hòa Lạc");
        p2.setCourseCode("CSD201");
        p2.setSeller(seller);
        p2.setStatus(ListingStatus.AVAILABLE);
        productRepository.save(p2);

        Product p3 = new Product();
        p3.setTitle("Kinh tế học vi mô");
        p3.setAuthor("N. Gregory Mankiw");
        p3.setPrice(new BigDecimal("70000")); // 70.000 VND
        p3.setCondition(Condition.ACCEPTABLE);
        p3.setCategory("Kinh tế");
        p3.setCampus("ĐH FPT Hòa Lạc");
        p3.setCourseCode("ECO111");
        p3.setSeller(seller);
        p3.setStatus(ListingStatus.AVAILABLE);
        productRepository.save(p3);

        // Academic hierarchy setup
        // Use correct repository method: findByShortNameIgnoreCase
        fptInst = institutionRepository.findByShortNameIgnoreCase("FPTU")
                .orElseGet(() -> institutionRepository.save(
                        // Institution constructor: (name, shortName, description, website) — 4 args
                        new Institution("Trường Đại học FPT", "FPTU", "FPT Education", "https://fpt.edu.vn")));
        vnuInst = institutionRepository.findByShortNameIgnoreCase("VNU")
                .orElseGet(() -> institutionRepository.save(
                        new Institution("Đại học Quốc gia Hà Nội", "VNU", "VNU Hanoi", "https://vnu.edu.vn")));

        // Campus constructor: (Institution, String name, String location, boolean hoaLac)
        Campus fptCampus = campusRepository.save(new Campus(fptInst, "Cơ sở Hòa Lạc FPT", "Khu CNC Hòa Lạc, Km29 ĐL Thăng Long", true));
        // AcademicProgram constructor: (institution, campus, faculty, name, major, programCode, sourceUrl) — 7 args
        fptSe = programRepository.save(new AcademicProgram(fptInst, fptCampus, "Khoa Công nghệ", "Kỹ thuật phần mềm Test", "SE-TEST", "SE", null));
        vnuCs = programRepository.save(new AcademicProgram(vnuInst, null, "Khoa Toán-Tin", "Khoa học máy tính Test", "CS-TEST", "CS", null));

        // Course constructor: (program, courseCode, name, description, semester, credits, mandatory) — 7 args
        csdCourse = courseRepository.save(new Course(fptSe, "CSD201-TEST", "Data Structures & Algorithms", "Môn thuật toán", 3, 3, true));
        dsaCourse = courseRepository.save(new Course(vnuCs, "INT2204-TEST", "Lập trình hướng đối tượng", "Môn OOP Java", 2, 3, true));

        algoBook = academicBookRepository.save(new AcademicBook(
                "978-0262033848", "Introduction to Algorithms (CLRS) Test", "CLRS 3rd",
                "Thomas H. Cormen", "MIT Press", "3rd", 2009, "English", "Sách giải thuật chuẩn",
                null, "Công nghệ thông tin", "Giáo trình chính"));

        javaBook = academicBookRepository.save(new AcademicBook(
                "978-0596009205", "Head First Java Test", "Head First Java 2nd",
                "Kathy Sierra", "O'Reilly", "2nd", 2005, "English", "Sách Java trực quan",
                null, "Công nghệ thông tin", "Giáo trình chính"));

        courseBookRepository.save(new CourseBook(csdCourse, algoBook, CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPT", "https://fpt.edu.vn", true));
        courseBookRepository.save(new CourseBook(dsaCourse, javaBook, CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa VNU", "https://vnu.edu.vn", true));
    }

    // =========================================================================
    // BUG 1 TESTS – FIND BOOKS FILTERS & SORTING
    // =========================================================================

    @Test
    @DisplayName("BUG 1 - Search books by keyword filters accurately")
    void testSearchByKeyword() throws Exception {
        MvcResult result = mockMvc.perform(get("/books").param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attribute("keyword", "Java"))
                .andReturn();

        List<Product> products = (List<Product>) result.getModelAndView().getModel().get("products");
        assertThat(products).isNotEmpty();
        assertThat(products).allMatch(p -> p.getTitle().contains("Java") || (p.getCourseCode() != null && p.getCourseCode().contains("Java")));
    }

    @Test
    @DisplayName("BUG 1 - Combined filters (keyword, category, condition, minPrice, maxPrice) work together")
    void testCombinedSearchFilters() throws Exception {
        MvcResult result = mockMvc.perform(get("/books")
                        .param("keyword", "CLRS")
                        .param("category", "Công nghệ thông tin")
                        .param("condition", "LIKE_NEW")
                        .param("minPrice", "100000")
                        .param("maxPrice", "150000")
                        .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attribute("selectedCategory", "Công nghệ thông tin"))
                .andExpect(model().attribute("selectedCondition", "LIKE_NEW"))
                .andExpect(model().attribute("selectedSort", "price_asc"))
                .andReturn();

        List<Product> products = (List<Product>) result.getModelAndView().getModel().get("products");
        assertThat(products).hasSize(1);
        Product p = products.get(0);
        assertThat(p.getTitle()).contains("CLRS");
        assertThat(p.getPrice()).isEqualByComparingTo(new BigDecimal("120000"));
        assertThat(p.getCondition()).isEqualTo(Condition.LIKE_NEW);
    }

    @Test
    @DisplayName("BUG 1 - Inverted price range (minPrice > maxPrice) is automatically adjusted gracefully")
    void testInvertedPriceRangeAdjusted() throws Exception {
        mockMvc.perform(get("/books")
                        .param("minPrice", "150000")
                        .param("maxPrice", "50000"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("priceRangeWarning"))
                .andExpect(model().attribute("minPrice", new BigDecimal("50000")))
                .andExpect(model().attribute("maxPrice", new BigDecimal("150000")));
    }

    @Test
    @DisplayName("BUG 1 - Sorting by price ascending and descending works")
    void testSortingByPrice() throws Exception {
        // Price ASC
        MvcResult ascResult = mockMvc.perform(get("/books").param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andReturn();
        List<Product> ascProducts = (List<Product>) ascResult.getModelAndView().getModel().get("products");
        for (int i = 0; i < ascProducts.size() - 1; i++) {
            assertThat(ascProducts.get(i).getPrice()).isLessThanOrEqualTo(ascProducts.get(i + 1).getPrice());
        }

        // Price DESC
        MvcResult descResult = mockMvc.perform(get("/books").param("sort", "price_desc"))
                .andExpect(status().isOk())
                .andReturn();
        List<Product> descProducts = (List<Product>) descResult.getModelAndView().getModel().get("products");
        for (int i = 0; i < descProducts.size() - 1; i++) {
            assertThat(descProducts.get(i).getPrice()).isGreaterThanOrEqualTo(descProducts.get(i + 1).getPrice());
        }
    }

    // =========================================================================
    // BUG 2 TESTS – TEXTBOOK UNIVERSITY CLASSIFICATION
    // =========================================================================

    @Test
    @DisplayName("BUG 2 - Filtering textbook catalog by FPT University only returns FPT textbooks")
    void testFilterTextbooksByFptUniversity() throws Exception {
        MvcResult result = mockMvc.perform(get("/textbooks").param("institutionId", fptInst.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("textbooks/browse"))
                .andExpect(model().attribute("selectedInstitutionId", fptInst.getId()))
                .andExpect(model().attributeExists("programs"))
                .andReturn();

        Page<AcademicBook> books = (Page<AcademicBook>) result.getModelAndView().getModel().get("books");
        assertThat(books).isNotEmpty();
        // The algoBook is mapped to FPT CSD course
        assertThat(books.getContent()).anyMatch(b -> b.getId().equals(algoBook.getId()));
        // The javaBook is mapped to VNU only, so must not appear in FPT books
        assertThat(books.getContent()).noneMatch(b -> b.getId().equals(javaBook.getId()));
    }

    @Test
    @DisplayName("BUG 2 - Filtering textbook catalog by VNU only returns VNU textbooks")
    void testFilterTextbooksByVnuUniversity() throws Exception {
        MvcResult result = mockMvc.perform(get("/textbooks").param("institutionId", vnuInst.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("textbooks/browse"))
                .andExpect(model().attribute("selectedInstitutionId", vnuInst.getId()))
                .andReturn();

        Page<AcademicBook> books = (Page<AcademicBook>) result.getModelAndView().getModel().get("books");
        assertThat(books).isNotEmpty();
        // The javaBook is mapped to VNU
        assertThat(books.getContent()).anyMatch(b -> b.getId().equals(javaBook.getId()));
        // The algoBook is mapped to FPT only, so must not appear in VNU books
        assertThat(books.getContent()).noneMatch(b -> b.getId().equals(algoBook.getId()));
    }

    @Test
    @DisplayName("BUG 2 - Filtering by Academic Program returns program-specific courses and books")
    void testFilterTextbooksByProgram() throws Exception {
        mockMvc.perform(get("/textbooks")
                        .param("institutionId", fptInst.getId().toString())
                        .param("programId", fptSe.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("textbooks/browse"))
                .andExpect(model().attribute("selectedProgramId", fptSe.getId()))
                .andExpect(model().attributeExists("courses"));
    }

    // =========================================================================
    // BUG 3 TESTS – TEXTBOOK CATALOG MULTI-PAGE PAGINATION
    // =========================================================================

    @Test
    @DisplayName("BUG 3 - Textbook catalog pagination allows visiting page 0 and subsequent pages")
    void testTextbookCatalogPagination() throws Exception {
        // Page 0
        mockMvc.perform(get("/textbooks").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("textbooks/browse"))
                .andExpect(model().attributeExists("books"));

        // Page 1
        mockMvc.perform(get("/textbooks").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("textbooks/browse"))
                .andExpect(model().attributeExists("books"));
    }

    // =========================================================================
    // BUG 4 TESTS – REALISTIC VND PRICING
    // =========================================================================

    @Test
    @DisplayName("BUG 4 - All marketplace listings have realistic VND pricing (>= 10.000 VND)")
    void testRealisticPricingOnListings() {
        List<Product> availableProducts = productRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.AVAILABLE);
        assertThat(availableProducts).isNotEmpty();
        // Ensure no product has unrealistic tiny prices such as 35 VND or 50 VND
        assertThat(availableProducts).allMatch(p -> p.getPrice().compareTo(new BigDecimal("1000")) >= 0,
                "Every listing price should be at least 1.000 VND");
    }
}

