package com.bookloop.controller;

import com.bookloop.entity.*;
import com.bookloop.repository.*;
import com.bookloop.service.PlatformSettingService;
import com.bookloop.service.PromotionService;
import com.bookloop.util.FormatUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EnhancementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionPaymentRepository promotionPaymentRepository;

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
    private AdminLogRepository adminLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlatformSettingService settingService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private FormatUtils formatUtils;

    private User seller;
    private User otherSeller;
    private User admin;
    private Institution fptu;
    private Course csd201;
    private AcademicBook dsaBook;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        settingService.setListingPromotionFee(new BigDecimal("10000"));
        settingService.setPromotionDurationDays(7);

        // 1. Create Users
        seller = new User();
        seller.setFullName("Nguyen Van Seller");
        seller.setEmail("seller@fpt.edu.vn");
        seller.setPassword(passwordEncoder.encode("pass123"));
        seller.setRole(Role.STUDENT);
        seller.setCampus("FPT Hòa Lạc");
        seller.setEnabled(true);
        seller = userRepository.save(seller);

        otherSeller = new User();
        otherSeller.setFullName("Tran Thi Other");
        otherSeller.setEmail("other@vnu.edu.vn");
        otherSeller.setPassword(passwordEncoder.encode("pass123"));
        otherSeller.setRole(Role.STUDENT);
        otherSeller.setCampus("VNU Hòa Lạc");
        otherSeller.setEnabled(true);
        otherSeller = userRepository.save(otherSeller);

        admin = new User();
        admin.setFullName("Admin Quan Tri");
        admin.setEmail("admin@bookloop.edu");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin = userRepository.save(admin);

        // 2. Create Institution, Program, Course, Academic Book
        fptu = new Institution();
        fptu.setName("Đại học FPT");
        fptu.setShortName("FPTU");
        fptu.setDescription("Khu Công nghệ cao Hòa Lạc");
        fptu.setActive(true);
        fptu = institutionRepository.save(fptu);

        Campus fptCampus = new Campus();
        fptCampus.setName("Khu CNC Hòa Lạc, Km29 Đại lộ Thăng Long");
        fptCampus.setInstitution(fptu);
        campusRepository.save(fptCampus);

        AcademicProgram seProgram = new AcademicProgram();
        seProgram.setName("Kỹ thuật phần mềm (Software Engineering)");
        seProgram.setProgramCode("SE");
        seProgram.setInstitution(fptu);
        seProgram = programRepository.save(seProgram);

        csd201 = new Course();
        csd201.setCourseCode("CSD201");
        csd201.setName("Data Structures and Algorithms");
        csd201.setProgram(seProgram);
        csd201.setSemester(3);
        csd201 = courseRepository.save(csd201);

        dsaBook = new AcademicBook();
        dsaBook.setTitle("Data Structures and Algorithm Analysis in Java");
        dsaBook.setAuthor("Mark Allen Weiss");
        dsaBook.setIsbn("978-0132576277");
        dsaBook.setEdition("3rd Edition");
        dsaBook.setCategory("Computer Science");
        dsaBook = academicBookRepository.save(dsaBook);

        CourseBook cb = new CourseBook();
        cb.setCourse(csd201);
        cb.setBook(dsaBook);
        cb.setRelationshipType(CourseBookRelationshipType.REQUIRED);
        courseBookRepository.save(cb);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        adminLogRepository.deleteAll();
        promotionPaymentRepository.deleteAll();
        productRepository.deleteAll();
        courseBookRepository.deleteAll();
        courseRepository.deleteAll();
        programRepository.deleteAll();
        campusRepository.deleteAll();
        institutionRepository.deleteAll();
        academicBookRepository.deleteAll();
        userRepository.deleteAll();
    }

    // =========================================================================
    // FEATURE 1: ACADEMIC TEXTBOOK CATALOG TESTS
    // =========================================================================

    @Test
    @DisplayName("Public user can browse /textbooks and view Hòa Lạc institutions")
    void testBrowseAcademicCatalog() throws Exception {
        mockMvc.perform(get("/textbooks"))
                .andExpect(status().isOk())
                .andExpect(view().name("textbooks/browse"))
                .andExpect(model().attributeExists("institutions"))
                .andExpect(model().attributeExists("books"));
    }

    @Test
    @DisplayName("Public user can filter /textbooks by institutionId")
    void testFilterCatalogByInstitution() throws Exception {
        mockMvc.perform(get("/textbooks").param("institutionId", fptu.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("textbooks/browse"))
                .andExpect(model().attribute("selectedInstitutionId", fptu.getId()))
                .andExpect(model().attributeExists("programs"));
    }

    @Test
    @DisplayName("Public user can view textbook detail /textbooks/{id} with matched used copies")
    void testViewTextbookDetailWithCopies() throws Exception {
        // Create an available used marketplace listing linked to this academic book
        Product listing = new Product();
        listing.setTitle("Pass lại sách CSD201 Mark Allen Weiss bản đẹp");
        listing.setAuthor("Mark Allen Weiss");
        listing.setPrice(new BigDecimal("60000"));
        listing.setCondition(Condition.LIKE_NEW);
        listing.setCategory("Computer Science");
        listing.setCampus("FPT Hòa Lạc");
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setSeller(seller);
        listing.setAcademicBook(dsaBook);
        productRepository.save(listing);

        mockMvc.perform(get("/textbooks/{id}", dsaBook.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("textbooks/detail"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("usedCopies"));
    }

    // =========================================================================
    // FEATURE 2: PAID LISTING PROMOTION & WORKFLOW TESTS
    // =========================================================================

    @Test
    @DisplayName("Seller creates a listing and a pending PromotionPayment snapshot is automatically created")
    @WithMockUser(username = "seller@fpt.edu.vn", roles = "STUDENT")
    void testListingCreationTriggersPromotionPayment() throws Exception {
        mockMvc.perform(post("/my-listings/new")
                        .with(csrf())
                        .param("title", "Giáo trình CSD201 FPTU")
                        .param("author", "Mark Allen Weiss")
                        .param("price", "50000")
                        .param("condition", "LIKE_NEW")
                        .param("campus", "FPT Hòa Lạc")
                        .param("category", "Computer Science")
                        .param("academicBookId", dsaBook.getId().toString()))
                .andExpect(status().is3xxRedirection());

        List<Product> products = productRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId());
        assertThat(products).isNotEmpty();
        Product savedProduct = products.get(0);
        assertThat(savedProduct.isPromoted()).isFalse();

        // Verify PromotionPayment record exists
        PromotionPayment payment = promotionService.getPaymentForListing(savedProduct.getId());
        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PromotionPaymentStatus.PENDING);
        assertThat(payment.getFeeAmount()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(payment.getDurationDays()).isEqualTo(7);
    }

    @Test
    @DisplayName("Seller submits transaction proof for promotion payment")
    @WithMockUser(username = "seller@fpt.edu.vn", roles = "STUDENT")
    void testSellerSubmitPromotionProof() throws Exception {
        Product listing = new Product();
        listing.setTitle("Sách Lập Trình Java");
        listing.setPrice(new BigDecimal("40000"));
        listing.setCondition(Condition.GOOD);
        listing.setCampus("FPT Hòa Lạc");
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setSeller(seller);
        listing = productRepository.save(listing);

        PromotionPayment payment = promotionService.createPaymentForNewListing(listing, seller);

        mockMvc.perform(post("/my-listings/{id}/promotion-payment", listing.getId())
                        .with(csrf())
                        .param("transactionReference", "VCB123456789")
                        .param("paymentNotes", "Đã chuyển khoản qua Vietcombank"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-listings/" + listing.getId() + "/promotion-payment"));

        PromotionPayment updated = promotionPaymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(updated.getTransactionReference()).isEqualTo("VCB123456789");
        assertThat(updated.getPaymentNotes()).isEqualTo("Đã chuyển khoản qua Vietcombank");
    }

    @Test
    @DisplayName("Other seller cannot access another seller's promotion payment page")
    @WithMockUser(username = "other@vnu.edu.vn", roles = "STUDENT")
    void testUnauthorizedSellerCannotAccessPromotionPayment() throws Exception {
        Product listing = new Product();
        listing.setTitle("Sách Riêng Của Seller 1");
        listing.setPrice(new BigDecimal("40000"));
        listing.setCondition(Condition.GOOD);
        listing.setCampus("FPT Hòa Lạc");
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setSeller(seller);
        listing = productRepository.save(listing);

        mockMvc.perform(get("/my-listings/{id}/promotion-payment", listing.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-listings"));
    }

    @Test
    @DisplayName("Admin verifies and approves promotion payment -> listing is promoted with valid duration")
    @WithMockUser(username = "admin@bookloop.edu", roles = "ADMIN")
    void testAdminApprovePromotionPayment() throws Exception {
        Product listing = new Product();
        listing.setTitle("Sách Cần Đẩy Nổi Bật");
        listing.setPrice(new BigDecimal("70000"));
        listing.setCondition(Condition.NEW);
        listing.setCampus("FPT Hòa Lạc");
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setSeller(seller);
        listing = productRepository.save(listing);

        PromotionPayment payment = promotionService.createPaymentForNewListing(listing, seller);
        promotionService.submitPaymentProof(payment.getId(), "MOMO987654", "Chuyển MoMo", seller);

        mockMvc.perform(post("/admin/promotion-payments/{id}/verify", payment.getId())
                        .with(csrf())
                        .param("approved", "true")
                        .param("adminNote", "Đã nhận đúng 10.000 đ"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/promotion-payments"));

        PromotionPayment approved = promotionPaymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(approved.getStatus()).isEqualTo(PromotionPaymentStatus.PAID);

        Product promotedProduct = productRepository.findById(listing.getId()).orElseThrow();
        assertThat(promotedProduct.isPromoted()).isTrue();
        assertThat(promotedProduct.getPromotedUntil()).isAfter(LocalDateTime.now().plusDays(6));

        // Audit log created
        List<AdminLog> logs = adminLogRepository.findAll();
        assertThat(logs).anyMatch(l -> l.getAction().equals("PROMOTION_PAYMENT_APPROVED"));
    }

    @Test
    @DisplayName("Admin can view and update promotion fee settings")
    @WithMockUser(username = "admin@bookloop.edu", roles = "ADMIN")
    void testAdminUpdateFeeSettings() throws Exception {
        mockMvc.perform(get("/admin/fee-settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/fee-settings"))
                .andExpect(model().attributeExists("currentFee"));

        mockMvc.perform(post("/admin/fee-settings")
                        .with(csrf())
                        .param("fee", "15000")
                        .param("durationDays", "10")
                        .param("instructions", "Chuyển khoản Vietinbank STK 101010"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/fee-settings"));

        assertThat(settingService.getListingPromotionFee()).isEqualByComparingTo(new BigDecimal("15000"));
        assertThat(settingService.getPromotionDurationDays()).isEqualTo(10);
    }

    // =========================================================================
    // FEATURE 3: VIETNAMESE LOCALIZATION & VND CURRENCY TESTS
    // =========================================================================

    @Test
    @DisplayName("FormatUtils correctly formats amounts into standard VND currency")
    void testFormatVnd() {
        assertThat(formatUtils.formatVnd(new BigDecimal("10000"))).isEqualTo("10.000 ₫");
        assertThat(formatUtils.formatVnd(new BigDecimal("50000"))).isEqualTo("50.000 ₫");
        assertThat(formatUtils.formatVnd(new BigDecimal("150000"))).isEqualTo("150.000 ₫");
        assertThat(formatUtils.formatVnd(null)).isEqualTo("0 ₫");
    }

    @Test
    @DisplayName("FormatUtils correctly formats LocalDateTime in Vietnam pattern")
    void testFormatDate() {
        LocalDateTime dt = LocalDateTime.of(2026, 11, 3, 14, 30);
        assertThat(formatUtils.formatDate(dt)).isEqualTo("03/11/2026");
        assertThat(formatUtils.formatDateTime(dt)).contains("03/11/2026");
    }
}
