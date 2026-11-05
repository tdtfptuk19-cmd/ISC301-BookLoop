package com.bookloop.controller;

import com.bookloop.entity.*;
import com.bookloop.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarketplaceListingsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AcademicBookRepository academicBookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromotionPaymentRepository paymentRepository;

    private User fptuSeller;
    private User vnuSeller;
    private User aofSeller;
    private AcademicBook cBook;
    private AcademicBook algoBook;
    private AcademicBook accBook;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        productRepository.deleteAll();

        fptuSeller = userRepository.findByEmailIgnoreCase("test.fptu@bookloop.vn")
                .orElseGet(() -> {
                    User u = new User();
                    u.setFullName("Nguyễn Hoàng Nam");
                    u.setEmail("test.fptu@bookloop.vn");
                    u.setPassword("hashedpassword123");
                    u.setUniversity("Trường Đại học FPT");
                    u.setCampus("FPT University Hòa Lạc");
                    u.setPhone("0981112233");
                    u.setRole(Role.STUDENT);
                    u.setEnabled(true);
                    return userRepository.save(u);
                });

        vnuSeller = userRepository.findByEmailIgnoreCase("test.vnu@bookloop.vn")
                .orElseGet(() -> {
                    User u = new User();
                    u.setFullName("Lê Thanh Thảo");
                    u.setEmail("test.vnu@bookloop.vn");
                    u.setPassword("hashedpassword123");
                    u.setUniversity("Đại học Quốc gia Hà Nội");
                    u.setCampus("VNU Hòa Lạc (Đô thị ĐHQGHN)");
                    u.setPhone("0982223344");
                    u.setRole(Role.STUDENT);
                    u.setEnabled(true);
                    return userRepository.save(u);
                });

        aofSeller = userRepository.findByEmailIgnoreCase("test.aof@bookloop.vn")
                .orElseGet(() -> {
                    User u = new User();
                    u.setFullName("Đỗ Mai Anh");
                    u.setEmail("test.aof@bookloop.vn");
                    u.setPassword("hashedpassword123");
                    u.setUniversity("Học viện Tài chính");
                    u.setCampus("Học viện Tài chính - Cơ sở Hòa Lạc");
                    u.setPhone("0983334455");
                    u.setRole(Role.STUDENT);
                    u.setEnabled(true);
                    return userRepository.save(u);
                });

        cBook = academicBookRepository.findByIsbn("978-0131103627")
                .orElseGet(() -> academicBookRepository.save(new AcademicBook(
                        "978-0131103627",
                        "The C Programming Language",
                        "The C Programming Language",
                        "Brian W. Kernighan, Dennis M. Ritchie",
                        "Prentice Hall",
                        "2nd",
                        1988,
                        "English",
                        "Classic reference manual for C programming.",
                        "https://m.media-amazon.com/images/I/411S7Ez9TTL._SX377_BO1,204,203,200_.jpg",
                        "Công nghệ thông tin",
                        "Giáo trình chính"
                )));

        algoBook = academicBookRepository.findByIsbn("978-0262033848")
                .orElseGet(() -> academicBookRepository.save(new AcademicBook(
                        "978-0262033848",
                        "Introduction to Algorithms",
                        "Introduction to Algorithms",
                        "Thomas H. Cormen",
                        "MIT Press",
                        "3rd",
                        2009,
                        "English",
                        "Comprehensive textbook on computer algorithms.",
                        "https://m.media-amazon.com/images/I/61ZXunAMYyL._SL1233_.jpg",
                        "Công nghệ thông tin",
                        "Giáo trình chính"
                )));

        accBook = academicBookRepository.findByIsbn("978-6049221111")
                .orElseGet(() -> academicBookRepository.save(new AcademicBook(
                        "978-6049221111",
                        "Giáo trình Nguyên lý kế toán (Học viện Tài chính)",
                        "Principles of Accounting",
                        "GS.TS. Ngô Thế Chi",
                        "NXB Tài chính",
                        "2021",
                        2021,
                        "Tiếng Việt",
                        "Giáo trình chuẩn mực nguyên lý kế toán Học viện Tài chính.",
                        "https://covers.openlibrary.org/b/isbn/9781118875056-L.jpg",
                        "Kế toán - Tài chính",
                        "Giáo trình chính"
                )));
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create marketplace listing linked to academic book with realistic VND pricing")
    void testCreateMarketplaceListingLinkedToAcademicBook() {
        Product p = new Product();
        p.setAcademicBook(cBook);
        p.setSeller(fptuSeller);
        p.setTitle(cBook.getTitle());
        p.setAuthor(cBook.getAuthor());
        p.setPublisher(cBook.getPublisher());
        p.setEdition(cBook.getEdition());
        p.setIsbn(cBook.getIsbn());
        p.setCourseCode("PRF192");
        p.setCategory("Công nghệ thông tin");
        p.setCampus(fptuSeller.getCampus());
        p.setPrice(new BigDecimal("115000"));
        p.setCondition(Condition.LIKE_NEW);
        p.setImageUrl(cBook.getCoverImageUrl());
        p.setDefectNotes("Sách đẹp 98%, không quăn mép, gáy sách chắc chắn.");
        p.setDescription("Sách chỉ dùng lướt qua một kỳ thi môn PRF192.");
        p.setStatus(ListingStatus.AVAILABLE);
        p.setPromoted(true);
        p.setPromotedUntil(LocalDateTime.now().plusDays(7));
        Product saved = productRepository.save(p);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAcademicBook()).isNotNull();
        assertThat(saved.getAcademicBook().getId()).isEqualTo(cBook.getId());
        assertThat(saved.getPrice()).isEqualByComparingTo("115000");
        assertThat(saved.getCondition()).isEqualTo(Condition.LIKE_NEW);
        assertThat(saved.getStatus()).isEqualTo(ListingStatus.AVAILABLE);

        // Verify retrieval by academic book
        List<Product> usedCopies = productRepository.findByAcademicBookIdAndStatusOrderByCreatedAtDesc(
                cBook.getId(), ListingStatus.AVAILABLE);
        assertThat(usedCopies).hasSize(1);
        assertThat(usedCopies.get(0).getTitle()).isEqualTo("The C Programming Language");
    }

    @Test
    @DisplayName("Should filter marketplace listings by campus, category, and price range")
    void testFilterByCampusCategoryAndPrice() {
        // Create 3 listings across universities
        Product p1 = createTestListing(cBook, fptuSeller, "PRF192", "Công nghệ thông tin",
                "FPT University Hòa Lạc", new BigDecimal("115000"), Condition.LIKE_NEW, true);
        Product p2 = createTestListing(algoBook, vnuSeller, "INT1008", "Công nghệ thông tin",
                "VNU Hòa Lạc (Đô thị ĐHQGHN)", new BigDecimal("70000"), Condition.ACCEPTABLE, false);
        Product p3 = createTestListing(accBook, aofSeller, "ACC101", "Kế toán - Tài chính",
                "Học viện Tài chính - Cơ sở Hòa Lạc", new BigDecimal("130000"), Condition.NEW, false);

        productRepository.saveAll(List.of(p1, p2, p3));

        // 1. Filter by Campus FPT
        Page<Product> fptResult = productRepository.searchAvailable(
                ListingStatus.AVAILABLE, null, null, "FPT University Hòa Lạc", null, null, null, PageRequest.of(0, 10));
        assertThat(fptResult.getContent()).hasSize(1);
        assertThat(fptResult.getContent().get(0).getCampus()).isEqualTo("FPT University Hòa Lạc");

        // 2. Filter by Category "Kế toán - Tài chính"
        Page<Product> aofCategoryResult = productRepository.searchAvailable(
                ListingStatus.AVAILABLE, null, "Kế toán - Tài chính", null, null, null, null, PageRequest.of(0, 10));
        assertThat(aofCategoryResult.getContent()).hasSize(1);
        assertThat(aofCategoryResult.getContent().get(0).getTitle()).contains("Nguyên lý kế toán");

        // 3. Filter by Price Range: 60,000 - 120,000
        Page<Product> priceResult = productRepository.searchAvailable(
                ListingStatus.AVAILABLE, null, null, null, null,
                new BigDecimal("60000"), new BigDecimal("120000"), PageRequest.of(0, 10));
        assertThat(priceResult.getContent()).hasSize(2); // p1 (115k) and p2 (70k)
    }

    @Test
    @DisplayName("Should prioritize promoted listings at the top of search results")
    void testPromotedListingsPrecedence() {
        Product regular = createTestListing(algoBook, vnuSeller, "INT1008", "Công nghệ thông tin",
                "VNU Hòa Lạc (Đô thị ĐHQGHN)", new BigDecimal("70000"), Condition.GOOD, false);
        Product promoted = createTestListing(cBook, fptuSeller, "PRF192", "Công nghệ thông tin",
                "FPT University Hòa Lạc", new BigDecimal("115000"), Condition.LIKE_NEW, true);

        productRepository.save(regular);
        productRepository.save(promoted);

        Page<Product> results = productRepository.searchAvailable(
                ListingStatus.AVAILABLE, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent().get(0).isPromoted()).isTrue();
        assertThat(results.getContent().get(1).isPromoted()).isFalse();
    }

    @Test
    @DisplayName("Should display linked used copies on textbook detail page")
    void testTextbookDetailPageShowsUsedCopies() throws Exception {
        Product p = createTestListing(cBook, fptuSeller, "PRF192", "Công nghệ thông tin",
                "FPT University Hòa Lạc", new BigDecimal("115000"), Condition.LIKE_NEW, true);
        productRepository.save(p);

        mockMvc.perform(get("/textbooks/" + cBook.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("usedCopies"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("The C Programming Language")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("115.000")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nguyễn Hoàng Nam")));
    }

    private Product createTestListing(AcademicBook book, User seller, String courseCode,
                                     String category, String campus, BigDecimal price,
                                     Condition condition, boolean isPromoted) {
        Product p = new Product();
        p.setAcademicBook(book);
        p.setSeller(seller);
        p.setTitle(book.getTitle());
        p.setAuthor(book.getAuthor());
        p.setPublisher(book.getPublisher());
        p.setEdition(book.getEdition());
        p.setIsbn(book.getIsbn());
        p.setCourseCode(courseCode);
        p.setCategory(category);
        p.setCampus(campus);
        p.setPrice(price);
        p.setCondition(condition);
        p.setImageUrl(book.getCoverImageUrl());
        p.setDefectNotes("Sách được giữ gìn cẩn thận, bìa sạch đẹp.");
        p.setDescription("Sách giáo trình chính quy theo chuẩn môn học.");
        p.setStatus(ListingStatus.AVAILABLE);
        p.setPromoted(isPromoted);
        if (isPromoted) {
            p.setPromotedUntil(LocalDateTime.now().plusDays(7));
        }
        return p;
    }
}
