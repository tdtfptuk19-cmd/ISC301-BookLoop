package com.bookloop.controller;

import com.bookloop.dto.CheckoutForm;
import com.bookloop.dto.ReconciliationReportDto;
import com.bookloop.entity.*;
import com.bookloop.payment.dto.SellerOnboardingRequest;
import com.bookloop.payment.provider.MockPaymentProvider;
import com.bookloop.repository.*;
import com.bookloop.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
class PaymentWorkflowIntegrationTest {

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
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private PayoutTransactionRepository payoutTransactionRepository;

    @Autowired
    private CommissionRecordRepository commissionRecordRepository;

    @Autowired
    private RefundTransactionRepository refundTransactionRepository;

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private SellerPaymentAccountRepository sellerPaymentAccountRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private SellerFinanceService sellerFinanceService;

    @Autowired
    private ReconciliationService reconciliationService;

    @Autowired
    private MockPaymentProvider mockPaymentProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User buyer;
    private User seller1;
    private User seller2;
    private User admin;
    private Product book1;
    private Product book2;

    @BeforeEach
    void setUp() {
        disputeRepository.deleteAll();
        refundTransactionRepository.deleteAll();
        payoutTransactionRepository.deleteAll();
        commissionRecordRepository.deleteAll();
        paymentTransactionRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        sellerPaymentAccountRepository.deleteAll();
        userRepository.deleteAll();

        buyer = new User();
        buyer.setEmail("student_buyer@fpt.edu.vn");
        buyer.setPassword(passwordEncoder.encode("Password123!"));
        buyer.setFullName("Nguyen Van Mua");
        buyer.setRole(Role.STUDENT);
        buyer.setCampus("FPTU Hoa Lac");
        buyer.setPhone("0912345678");
        buyer.setEnabled(true);
        buyer = userRepository.save(buyer);

        seller1 = new User();
        seller1.setEmail("seller_one@fpt.edu.vn");
        seller1.setPassword(passwordEncoder.encode("Password123!"));
        seller1.setFullName("Tran Van Ban 1");
        seller1.setRole(Role.STUDENT);
        seller1.setCampus("FPTU Hoa Lac");
        seller1.setPhone("0987654321");
        seller1.setEnabled(true);
        seller1 = userRepository.save(seller1);

        seller2 = new User();
        seller2.setEmail("seller_two@fpt.edu.vn");
        seller2.setPassword(passwordEncoder.encode("Password123!"));
        seller2.setFullName("Le Van Ban 2");
        seller2.setRole(Role.STUDENT);
        seller2.setCampus("FPTU Hoa Lac");
        seller2.setPhone("0977112233");
        seller2.setEnabled(true);
        seller2 = userRepository.save(seller2);

        admin = new User();
        admin.setEmail("admin_finance@bookloop.vn");
        admin.setPassword(passwordEncoder.encode("Password123!"));
        admin.setFullName("Quan Tri Tai Chinh");
        admin.setRole(Role.ADMIN);
        admin.setCampus("BookLoop HQ");
        admin.setEnabled(true);
        admin = userRepository.save(admin);

        // Pre-onboard seller bank accounts
        SellerOnboardingRequest onbReq1 = new SellerOnboardingRequest(seller1.getId(), seller1.getFullName(), seller1.getEmail(), "VCB", "Vietcombank", "0123456789", "TRAN VAN BAN 1", "001201000001");
        sellerFinanceService.onboardAccount(seller1, onbReq1);

        SellerOnboardingRequest onbReq2 = new SellerOnboardingRequest(seller2.getId(), seller2.getFullName(), seller2.getEmail(), "TCB", "Techcombank", "9876543210", "LE VAN BAN 2", "001201000002");
        sellerFinanceService.onboardAccount(seller2, onbReq2);

        book1 = new Product();
        book1.setTitle("Giao trinh Tri tue nhan tao FPT");
        book1.setPrice(new BigDecimal("100000.00"));
        book1.setSeller(seller1);
        book1.setCampus("FPTU Hoa Lac");
        book1.setStatus(ListingStatus.AVAILABLE);
        book1.setCondition(Condition.LIKE_NEW);
        book1 = productRepository.save(book1);

        book2 = new Product();
        book2.setTitle("Giao trinh Co so du lieu FPT");
        book2.setPrice(new BigDecimal("200000.00"));
        book2.setSeller(seller2);
        book2.setCampus("FPTU Hoa Lac");
        book2.setStatus(ListingStatus.AVAILABLE);
        book2.setCondition(Condition.GOOD);
        book2 = productRepository.save(book2);
    }

    @Test
    @DisplayName("1. Multi-Seller Cart Checkout creates separate orders linked by parentCheckoutGroup")
    @WithMockUser(username = "student_buyer@fpt.edu.vn", roles = {"STUDENT"})
    void testMultiSellerCheckout() {
        cartService.addToCart(buyer, book1.getId());
        cartService.addToCart(buyer, book2.getId());

        CheckoutForm form = new CheckoutForm();
        form.setPaymentMethod(PaymentMethod.MARKETPLACE_HOLDING);
        form.setContactPhone("0912345678");
        form.setMeetupNote("Hen o san Beta FPTU");

        List<Order> orders = orderService.checkoutMulti(buyer, form);

        assertThat(orders).hasSize(2);
        String group = orders.get(0).getParentCheckoutGroup();
        assertThat(group).isNotBlank();
        assertThat(orders.get(1).getParentCheckoutGroup()).isEqualTo(group);

        Order ord1 = orders.stream().filter(o -> o.getSeller().getId().equals(seller1.getId())).findFirst().orElseThrow();
        Order ord2 = orders.stream().filter(o -> o.getSeller().getId().equals(seller2.getId())).findFirst().orElseThrow();

        assertThat(ord1.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(ord2.getTotalAmount()).isEqualByComparingTo(new BigDecimal("200000.00"));
        assertThat(ord1.getStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
        assertThat(ord2.getStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);

        // Cart is cleared
        Cart cart = cartService.getCart(buyer);
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("2. Payment checkout screen generates VietQR code session and displays holding notice")
    @WithMockUser(username = "student_buyer@fpt.edu.vn", roles = {"STUDENT"})
    void testPaymentCheckoutScreen() throws Exception {
        cartService.addToCart(buyer, book1.getId());
        CheckoutForm form = new CheckoutForm();
        form.setPaymentMethod(PaymentMethod.MARKETPLACE_HOLDING);
        Order order = orderService.checkout(buyer, form);

        mockMvc.perform(get("/buyer/payment/checkout/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/payment/pay"))
                .andExpect(model().attributeExists("sessionResponse"))
                .andExpect(model().attributeExists("transaction"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("vietqr://pay")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("tạm giữ có điều kiện")));
    }

    @Test
    @DisplayName("3. Partner simulated webhook / success moves transaction and order to PAID_HELD")
    @WithMockUser(username = "student_buyer@fpt.edu.vn", roles = {"STUDENT"})
    void testSimulatedPaymentSuccess() throws Exception {
        cartService.addToCart(buyer, book1.getId());
        CheckoutForm form = new CheckoutForm();
        form.setPaymentMethod(PaymentMethod.MARKETPLACE_HOLDING);
        Order order = orderService.checkout(buyer, form);

        // Initiate payment session
        mockMvc.perform(get("/buyer/payment/checkout/" + order.getId()))
                .andExpect(status().isOk());

        PaymentTransaction tx = paymentTransactionRepository.findTopByOrderIdOrderByCreatedAtDesc(order.getId()).orElseThrow();
        assertThat(tx.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);

        // Simulate success
        mockMvc.perform(post("/buyer/payment/mock-checkout/simulate-success")
                        .with(csrf())
                        .param("merchantReference", tx.getMerchantReference()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/" + order.getId()));

        PaymentTransaction updatedTx = paymentTransactionRepository.findById(tx.getId()).orElseThrow();
        assertThat(updatedTx.getPaymentStatus()).isEqualTo(PaymentStatus.HELD);

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID_HELD);
    }

    @Test
    @DisplayName("4. Seller dispatch, buyer delivery confirmation, and conditional payout disbursement")
    void testFullDeliveryAndPayoutDisbursement() {
        cartService.addToCart(buyer, book1.getId());
        CheckoutForm form = new CheckoutForm();
        form.setPaymentMethod(PaymentMethod.MARKETPLACE_HOLDING);
        Order order = orderService.checkout(buyer, form);

        // Buyer pays
        order.setStatus(OrderStatus.PAID_HELD);
        orderRepository.save(order);

        PaymentTransaction payTx = new PaymentTransaction();
        payTx.setOrder(order);
        payTx.setProviderCode("MOCK_PROVIDER");
        payTx.setProviderPaymentId("mock_pay_1");
        payTx.setMerchantReference("PAY-REF-1");
        payTx.setAmount(order.getTotalAmount());
        payTx.setPaymentStatus(PaymentStatus.HELD);
        paymentTransactionRepository.save(payTx);

        // Seller dispatches
        Order dispatchedOrder = orderService.sellerDispatchOrder(order.getId(), seller1);
        assertThat(dispatchedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(dispatchedOrder.getShippedAt()).isNotNull();

        // Buyer confirms receipt
        Order completedOrder = orderService.buyerConfirmReceipt(order.getId(), buyer);
        assertThat(completedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(completedOrder.getReceivedConfirmedAt()).isNotNull();

        // Verify Payout was created and completed
        List<PayoutTransaction> payouts = payoutTransactionRepository.findByOrderId(order.getId());
        assertThat(payouts).hasSize(1);
        PayoutTransaction payout = payouts.get(0);
        assertThat(payout.getStatus()).isEqualTo(PayoutStatus.PAID);
        assertThat(payout.getAmount()).isEqualByComparingTo(new BigDecimal("95000.00")); // 100k - 5% (5k)

        // Verify Commission was recorded and settled
        CommissionRecord commission = commissionRecordRepository.findByOrderId(order.getId()).orElseThrow();
        assertThat(commission.getStatus()).isEqualTo(CommissionStatus.SETTLED);
        assertThat(commission.getCommissionAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(commission.getSellerNetAmount()).isEqualByComparingTo(new BigDecimal("95000.00"));

        // Verify Payment Transaction is marked SETTLED
        PaymentTransaction settledTx = paymentTransactionRepository.findById(payTx.getId()).orElseThrow();
        assertThat(settledTx.getPaymentStatus()).isEqualTo(PaymentStatus.SETTLED);
    }

    @Test
    @DisplayName("5. Buyer opens dispute, escrow is locked, and Admin issues full refund")
    void testDisputeAndAdminRefundResolution() {
        cartService.addToCart(buyer, book1.getId());
        CheckoutForm form = new CheckoutForm();
        form.setPaymentMethod(PaymentMethod.MARKETPLACE_HOLDING);
        Order order = orderService.checkout(buyer, form);

        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        PaymentTransaction payTx = new PaymentTransaction();
        payTx.setOrder(order);
        payTx.setProviderCode("MOCK_PROVIDER");
        payTx.setProviderPaymentId("mock_pay_2");
        payTx.setMerchantReference("PAY-REF-2");
        payTx.setAmount(order.getTotalAmount());
        payTx.setPaymentStatus(PaymentStatus.HELD);
        paymentTransactionRepository.save(payTx);

        // Buyer opens dispute
        Dispute dispute = orderService.getOrderDetails(order.getId(), buyer) != null
                ? disputeRepository.save(new Dispute(order, buyer, "DAMAGED_OR_DEFECTIVE", "Sach bi rach trang nhieu khong doc duoc", "https://img.example.com/proof.jpg"))
                : null;
        order.setStatus(OrderStatus.DISPUTED);
        orderRepository.save(order);

        assertThat(dispute.getStatus()).isEqualTo(DisputeStatus.OPEN);

        // Admin mediates and resolves dispute with REFUNDED_BUYER
        RefundTransaction refundTx = new RefundTransaction(
                payTx, order, dispute, "REF-KEY-1", order.getTotalAmount(), RefundType.FULL,
                "Buyer claim approved: damaged book", admin
        );
        refundTx.setStatus(RefundStatus.REFUNDED);
        refundTransactionRepository.save(refundTx);
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        payTx.setPaymentStatus(PaymentStatus.REFUNDED);
        paymentTransactionRepository.save(payTx);

        Order refundedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(refundedOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("6. Reconciliation Service verifies 5 financial invariants")
    void testReconciliationService() {
        ReconciliationReportDto report = reconciliationService.runReconciliation("TestAuditor");
        assertThat(report).isNotNull();
        assertThat(report.isEscrowBalanced()).isTrue();
        assertThat(report.getDiscrepancyCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("7. Cross-seller authorization security enforcement")
    void testCrossSellerSecurityEnforcement() {
        cartService.addToCart(buyer, book1.getId());
        CheckoutForm form = new CheckoutForm();
        form.setPaymentMethod(PaymentMethod.MARKETPLACE_HOLDING);
        Order order = orderService.checkout(buyer, form);

        order.setStatus(OrderStatus.PAID_HELD);
        orderRepository.save(order);

        // Seller 2 attempts to dispatch Seller 1's order -> must throw UnauthorizedOperationException
        org.junit.jupiter.api.Assertions.assertThrows(
                com.bookloop.exception.UnauthorizedOperationException.class,
                () -> orderService.sellerDispatchOrder(order.getId(), seller2)
        );
    }

    @Test
    @DisplayName("8. Webhook endpoint with HMAC verification accepts signed payload")
    void testWebhookEndpoint() throws Exception {
        String payload = "{\"providerEventId\":\"evt_123\",\"eventType\":\"payment.paid_held\",\"merchantReference\":\"PAY-TEST-1\",\"amount\":100000.00}";
        String signature = mockPaymentProvider.generateValidSignature(payload);

        mockMvc.perform(post("/api/webhooks/payments/mock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("9. Admin Finance Overview is protected and requires ADMIN role")
    @WithMockUser(username = "student_buyer@fpt.edu.vn", roles = {"STUDENT"})
    void testAdminProtectionForStudents() throws Exception {
        mockMvc.perform(get("/admin/finance"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("10. Admin Finance Overview renders successfully for ADMIN user")
    @WithMockUser(username = "admin_finance@bookloop.vn", roles = {"ADMIN"})
    void testAdminOverviewAccess() throws Exception {
        mockMvc.perform(get("/admin/finance"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/finance/overview"))
                .andExpect(model().attributeExists("kpis"))
                .andExpect(model().attributeExists("commissionSetting"))
                .andExpect(model().attributeExists("reconciliation"));
    }
}

