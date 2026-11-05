package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.entity.Order;
import com.bookloop.entity.OrderStatus;
import com.bookloop.entity.PaymentTransaction;
import com.bookloop.entity.User;
import com.bookloop.payment.dto.PaymentSessionResponse;
import com.bookloop.payment.provider.MockPaymentProvider;
import com.bookloop.repository.PaymentTransactionRepository;
import com.bookloop.service.OrderService;
import com.bookloop.service.PaymentManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller managing buyer payment workflows: session initiation, VietQR display,
 * simulated sandbox checkout, and return handling.
 */
@Controller
@RequestMapping("/buyer/payment")
public class BuyerPaymentController {

    private final OrderService orderService;
    private final PaymentManagerService paymentManagerService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final MockPaymentProvider mockPaymentProvider;
    private final AuthenticatedUserResolver authResolver;

    public BuyerPaymentController(OrderService orderService,
                                  PaymentManagerService paymentManagerService,
                                  PaymentTransactionRepository paymentTransactionRepository,
                                  MockPaymentProvider mockPaymentProvider,
                                  AuthenticatedUserResolver authResolver) {
        this.orderService = orderService;
        this.paymentManagerService = paymentManagerService;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.mockPaymentProvider = mockPaymentProvider;
        this.authResolver = authResolver;
    }

    /**
     * Renders payment checkout screen with VietQR code and escrow holding explanation.
     */
    @GetMapping("/checkout/{orderId}")
    public String showPaymentCheckout(@PathVariable Long orderId,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        Order order = orderService.getOrderDetails(orderId, user);

        if (!order.isBuyer(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thanh toán cho đơn hàng này.");
            return "redirect:/orders/" + orderId;
        }

        if (order.getStatus() == OrderStatus.PAID_HELD ||
                order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.COMPLETED) {
            redirectAttributes.addFlashAttribute("infoMessage", "Đơn hàng này đã được thanh toán an toàn.");
            return "redirect:/orders/" + orderId;
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng này đã bị hủy, không thể thanh toán.");
            return "redirect:/orders/" + orderId;
        }

        String returnUrl = "/buyer/payment/return?orderId=" + order.getId();
        String cancelUrl = "/orders/" + order.getId();

        PaymentSessionResponse sessionResponse = paymentManagerService.initiateCheckoutSession(order, returnUrl, cancelUrl);
        Optional<PaymentTransaction> txOpt = paymentTransactionRepository.findByMerchantReference(sessionResponse.getMerchantReference());

        model.addAttribute("order", order);
        model.addAttribute("sessionResponse", sessionResponse);
        model.addAttribute("transaction", txOpt.orElse(null));
        return "buyer/payment/pay";
    }

    /**
     * Renders simulated partner payment screen for Sandbox/Demo environment.
     */
    @GetMapping("/mock-checkout")
    public String showMockCheckout(@RequestParam("ref") String merchantReference,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        Optional<PaymentTransaction> txOpt = paymentTransactionRepository.findByMerchantReference(merchantReference);
        if (txOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giao dịch thanh toán.");
            return "redirect:/orders/my-purchases";
        }

        PaymentTransaction tx = txOpt.get();
        model.addAttribute("transaction", tx);
        model.addAttribute("order", tx.getOrder());
        return "buyer/payment/mock-checkout";
    }

    /**
     * Simulates buyer completing payment on the partner gateway.
     * Transitions payment to PAID_HELD and order to PAID_HELD.
     */
    @PostMapping("/mock-checkout/simulate-success")
    public String simulatePaymentSuccess(@RequestParam("merchantReference") String merchantReference,
                                         RedirectAttributes redirectAttributes) {
        Optional<PaymentTransaction> txOpt = paymentTransactionRepository.findByMerchantReference(merchantReference);
        if (txOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giao dịch không tồn tại.");
            return "redirect:/orders/my-purchases";
        }

        PaymentTransaction tx = txOpt.get();
        mockPaymentProvider.markTransactionPaid(merchantReference);
        paymentManagerService.handlePaymentPaidHeld(merchantReference, tx.getProviderPaymentId(), tx.getAmount());

        redirectAttributes.addFlashAttribute("successMessage",
                "Thanh toán thành công! Tiền đang được đối tác thanh toán tạm giữ có điều kiện. Người bán sẽ chuẩn bị và giao giáo trình cho bạn.");
        return "redirect:/orders/" + tx.getOrder().getId();
    }

    /**
     * Return endpoint from partner gateway.
     */
    @GetMapping("/return")
    public String paymentReturn(@RequestParam(value = "orderId", required = false) Long orderId,
                                RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMessage", "Đang xử lý kết quả thanh toán...");
        return "redirect:/orders/" + (orderId != null ? orderId : "my-purchases");
    }
}

