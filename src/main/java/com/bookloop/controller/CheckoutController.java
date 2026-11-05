package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.dto.CheckoutForm;
import com.bookloop.entity.Cart;
import com.bookloop.entity.ListingStatus;
import com.bookloop.entity.Order;
import com.bookloop.entity.PaymentMethod;
import com.bookloop.entity.User;
import com.bookloop.service.CartService;
import com.bookloop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling checkout and order creation.
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final AuthenticatedUserResolver authResolver;

    public CheckoutController(CartService cartService,
                              OrderService orderService,
                              AuthenticatedUserResolver authResolver) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.authResolver = authResolver;
    }

    /**
     * Displays the checkout summary page.
     */
    @GetMapping
    public String showCheckout(Model model, RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        Cart cart = cartService.getCart(user);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống. Vui lòng chọn sách trước khi tiến hành thanh toán.");
            return "redirect:/cart";
        }

        // Verify if any items are no longer available
        boolean hasUnavailable = cart.getItems().stream()
                .anyMatch(item -> item.getProduct() == null
                        || item.getProduct().getStatus() != ListingStatus.AVAILABLE);

        if (hasUnavailable) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Một số cuốn sách trong giỏ hàng không còn sẵn sàng. Vui lòng kiểm tra và xóa sản phẩm không sẵn sàng trước khi thanh toán.");
            return "redirect:/cart";
        }

        CheckoutForm form = new CheckoutForm();
        if (user.getPhone() != null) {
            form.setContactPhone(user.getPhone());
        }

        model.addAttribute("cart", cart);
        model.addAttribute("totalAmount", cart.calculateTotal());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("checkoutForm", form);
        return "checkout/checkout";
    }

    /**
     * Processes checkout submission and creates the order.
     */
    @PostMapping
    public String processCheckout(
            @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = authResolver.getCurrentUser();
        Cart cart = cartService.getCart(user);

        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", cart);
            model.addAttribute("totalAmount", cart.calculateTotal());
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "checkout/checkout";
        }

        try {
            java.util.List<Order> orders = orderService.checkoutMulti(user, form);
            Order firstOrder = orders.get(0);

            if (form.getPaymentMethod() == PaymentMethod.MARKETPLACE_HOLDING) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã tạo đơn hàng! Vui lòng tiến hành thanh toán giữ tiền an toàn qua cổng liên kết.");
                return "redirect:/buyer/payment/checkout/" + firstOrder.getId();
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt hàng thành công! Mã đơn hàng #" + firstOrder.getOrderNumber());
            return "redirect:/orders/" + firstOrder.getId();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/checkout";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Đã xảy ra lỗi trong quá trình thanh toán: " + ex.getMessage());
            return "redirect:/checkout";
        }
    }
}
