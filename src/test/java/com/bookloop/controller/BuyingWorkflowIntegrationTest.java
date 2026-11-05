package com.bookloop.controller;

import com.bookloop.entity.*;
import com.bookloop.repository.*;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BuyingWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AcademicBookRepository academicBookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User seller;
    private User buyer;
    private Product availableProduct;
    private Product soldProduct;
    private Product hiddenProduct;

    @BeforeEach
    void setUp() {
        // Create Seller
        seller = userRepository.findByEmailIgnoreCase("seller_test@fpt.edu.vn").orElseGet(() -> {
            User u = new User();
            u.setEmail("seller_test@fpt.edu.vn");
            u.setPassword(passwordEncoder.encode("Password123"));
            u.setFullName("Nguyễn Văn Bán");
            u.setRole(Role.STUDENT);
            u.setCampus("Hòa Lạc");
            u.setEnabled(true);
            return userRepository.save(u);
        });

        // Create Buyer
        buyer = userRepository.findByEmailIgnoreCase("buyer_test@fpt.edu.vn").orElseGet(() -> {
            User u = new User();
            u.setEmail("buyer_test@fpt.edu.vn");
            u.setPassword(passwordEncoder.encode("Password123"));
            u.setFullName("Trần Thị Mua");
            u.setRole(Role.STUDENT);
            u.setCampus("Hòa Lạc");
            u.setPhone("0987654321");
            u.setEnabled(true);
            return userRepository.save(u);
        });

        // Academic Book
        AcademicBook ab = new AcademicBook();
        ab.setTitle("Lập trình Java căn bản và nâng cao");
        ab.setIsbn("978-604-0-12345-6");
        ab.setAuthor("Đoàn Văn Ban");
        ab.setPublisher("NXB Giáo Dục");
        ab.setEdition("Tái bản lần 3");
        ab.setPublicationYear(2023);
        ab.setCategory("Công nghệ thông tin");
        ab.setBookType("Giáo trình chính");
        ab = academicBookRepository.save(ab);

        // Available Product
        availableProduct = new Product();
        availableProduct.setTitle("Giáo trình Java cơ bản PRJ301");
        availableProduct.setDescription("Sách còn rất mới, giữ gìn cẩn thận, không quăn mép.");
        availableProduct.setPrice(new BigDecimal("120000"));
        availableProduct.setCondition(Condition.LIKE_NEW);
        availableProduct.setCategory("Công nghệ thông tin");
        availableProduct.setAuthor("Đoàn Văn Ban");
        availableProduct.setPublisher("NXB Giáo Dục");
        availableProduct.setEdition("Tái bản lần 3");
        availableProduct.setIsbn("978-604-0-12345-6");
        availableProduct.setCourseCode("PRJ301");
        availableProduct.setCampus("Hòa Lạc");
        availableProduct.setStatus(ListingStatus.AVAILABLE);
        availableProduct.setSeller(seller);
        availableProduct.setAcademicBook(ab);
        availableProduct.setCreatedAt(LocalDateTime.now());
        availableProduct = productRepository.save(availableProduct);

        // Sold Product
        soldProduct = new Product();
        soldProduct.setTitle("Giáo trình Cấu trúc dữ liệu CSD201");
        soldProduct.setDescription("Đã học xong kỳ trước pass môn.");
        soldProduct.setPrice(new BigDecimal("95000"));
        soldProduct.setCondition(Condition.GOOD);
        soldProduct.setCategory("Công nghệ thông tin");
        soldProduct.setCampus("Hòa Lạc");
        soldProduct.setStatus(ListingStatus.SOLD);
        soldProduct.setSeller(seller);
        soldProduct.setCreatedAt(LocalDateTime.now());
        soldProduct = productRepository.save(soldProduct);

        // Hidden Product
        hiddenProduct = new Product();
        hiddenProduct.setTitle("Tài liệu ôn tập nháp chưa kiểm duyệt");
        hiddenProduct.setDescription("Chờ duyệt");
        hiddenProduct.setPrice(new BigDecimal("50000"));
        hiddenProduct.setCondition(Condition.ACCEPTABLE);
        hiddenProduct.setCategory("Công nghệ thông tin");
        hiddenProduct.setCampus("Hòa Lạc");
        hiddenProduct.setStatus(ListingStatus.HIDDEN);
        hiddenProduct.setSeller(seller);
        hiddenProduct.setCreatedAt(LocalDateTime.now());
        hiddenProduct = productRepository.save(hiddenProduct);
    }

    @Test
    @DisplayName("BUG A: Book details page displays complete information, action buttons, and related books")
    @WithMockUser(username = "buyer_test@fpt.edu.vn", roles = {"STUDENT"})
    void testShowBookDetail_AvailableProduct_BuyerPerspective() throws Exception {
        mockMvc.perform(get("/books/" + availableProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("books/detail"))
                .andExpect(model().attributeExists("product", "relatedBooks", "isOwner"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Giáo trình Java cơ bản PRJ301")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Đoàn Văn Ban")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PRJ301")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Khớp với giáo trình chính thức trong mục lục:")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Lập trình Java căn bản và nâng cao")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Thêm vào giỏ hàng")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mua ngay")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sẵn sàng giao dịch")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Xem hồ sơ người bán")));
    }

    @Test
    @DisplayName("BUG A: Sold book detail shows 'Sản phẩm đã hết hàng' and suppresses buy actions")
    @WithMockUser(username = "buyer_test@fpt.edu.vn", roles = {"STUDENT"})
    void testShowBookDetail_SoldProduct() throws Exception {
        mockMvc.perform(get("/books/" + soldProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("books/detail"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sản phẩm đã hết hàng")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cuốn sách này đã được bán thành công.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<button type=\"submit\" class=\"btn-primary-bl w-100 justify-content-center\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Mua ngay"))));
    }

    @Test
    @DisplayName("BUG A: Hidden book detail shows 'Sách chưa được duyệt' alert")
    @WithMockUser(username = "buyer_test@fpt.edu.vn", roles = {"STUDENT"})
    void testShowBookDetail_HiddenProduct() throws Exception {
        mockMvc.perform(get("/books/" + hiddenProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("books/detail"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sách chưa được duyệt")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tin đăng này hiện đang tạm ẩn hoặc đang chờ kiểm duyệt từ quản trị viên.")));
    }

    @Test
    @DisplayName("BUG B: Unauthenticated guest adding book redirects to login")
    void testAddToCart_GuestRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .param("productId", availableProduct.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("BUG B: Authenticated user successfully adds book to cart")
    @WithMockUser(username = "buyer_test@fpt.edu.vn", roles = {"STUDENT"})
    void testAddToCart_Success() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .param("productId", availableProduct.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"))
                .andExpect(flash().attribute("successMessage", "Đã thêm vào giỏ hàng"));

        Cart cart = cartRepository.findByUserIdWithItems(buyer.getId()).orElse(null);
        assertThat(cart).isNotNull();
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProduct().getId()).isEqualTo(availableProduct.getId());
    }

    @Test
    @DisplayName("BUG B: Seller cannot add their own book to cart")
    @WithMockUser(username = "seller_test@fpt.edu.vn", roles = {"STUDENT"})
    void testAddToCart_CannotAddOwnProduct() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .param("productId", availableProduct.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + availableProduct.getId()))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("BUG B: Cannot add sold book to cart")
    @WithMockUser(username = "buyer_test@fpt.edu.vn", roles = {"STUDENT"})
    void testAddToCart_CannotAddSoldProduct() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .param("productId", soldProduct.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + soldProduct.getId()))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("BUG B: 'Mua ngay' adds book and redirects directly to checkout")
    @WithMockUser(username = "buyer_test@fpt.edu.vn", roles = {"STUDENT"})
    void testBuyNow_RedirectsDirectlyToCheckout() throws Exception {
        mockMvc.perform(post("/cart/buy-now")
                        .with(csrf())
                        .param("productId", availableProduct.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout"));

        Cart cart = cartRepository.findByUserIdWithItems(buyer.getId()).orElse(null);
        assertThat(cart).isNotNull();
        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("BUG B: Cart view displays correct Vietnamese labels: 'Tiếp tục mua sắm' and 'Tiến hành thanh toán'")
    @WithMockUser(username = "buyer_test@fpt.edu.vn", roles = {"STUDENT"})
    void testCartView_Labels() throws Exception {
        // First add product to cart
        mockMvc.perform(post("/cart/add")
                .with(csrf())
                .param("productId", availableProduct.getId().toString()));

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/view"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tiếp tục mua sắm")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tiến hành thanh toán")));
    }

    @Test
    @DisplayName("BUG B: Checkout creates order, snapshots price, transitions book to SOLD, and clears cart")
    @WithMockUser(username = "buyer_test@fpt.edu.vn", roles = {"STUDENT"})
    void testCheckout_PlaceOrderSuccess() throws Exception {
        // 1. Add product to cart
        mockMvc.perform(post("/cart/add")
                .with(csrf())
                .param("productId", availableProduct.getId().toString()));

        // 2. Perform checkout
        MvcResult checkoutResult = mockMvc.perform(post("/checkout")
                        .with(csrf())
                        .param("paymentMethod", "IN_PERSON_MEETUP")
                        .param("contactPhone", "0987654321")
                        .param("meetupNote", "Gặp tại sảnh thư viện Alpha FPT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*"))
                .andReturn();

        String redirectedUrl = checkoutResult.getResponse().getRedirectedUrl();
        Long orderId = Long.parseLong(redirectedUrl.substring(redirectedUrl.lastIndexOf('/') + 1));

        // 3. Verify Order
        Order order = orderRepository.findById(orderId).orElse(null);
        assertThat(order).isNotNull();
        assertThat(order.getBuyer().getId()).isEqualTo(buyer.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("120000.00"));
        assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.IN_PERSON_MEETUP);
        assertThat(order.getItems()).hasSize(1);

        OrderItem orderItem = order.getItems().get(0);
        assertThat(orderItem.getPriceAtPurchase()).isEqualByComparingTo(new BigDecimal("120000.00"));
        assertThat(orderItem.getSeller().getId()).isEqualTo(seller.getId());

        // 4. Verify Product is now SOLD
        Product freshProduct = productRepository.findById(availableProduct.getId()).orElse(null);
        assertThat(freshProduct).isNotNull();
        assertThat(freshProduct.getStatus()).isEqualTo(ListingStatus.SOLD);

        // 5. Verify Buyer's Cart is empty
        Cart freshCart = cartRepository.findByUserIdWithItems(buyer.getId()).orElse(null);
        assertThat(freshCart == null || freshCart.getItems().isEmpty()).isTrue();
    }
}

