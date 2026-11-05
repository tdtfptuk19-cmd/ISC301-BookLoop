package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.dto.ReviewForm;
import com.bookloop.entity.Order;
import com.bookloop.entity.OrderItem;
import com.bookloop.entity.OrderStatus;
import com.bookloop.entity.User;
import com.bookloop.service.OrderService;
import com.bookloop.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller handling buyer and seller order management, status updates, and details.
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ReviewService reviewService;
    private final com.bookloop.service.DisputeRefundService disputeRefundService;
    private final AuthenticatedUserResolver authResolver;

    public OrderController(OrderService orderService,
                           ReviewService reviewService,
                           com.bookloop.service.DisputeRefundService disputeRefundService,
                           AuthenticatedUserResolver authResolver) {
        this.orderService = orderService;
        this.reviewService = reviewService;
        this.disputeRefundService = disputeRefundService;
        this.authResolver = authResolver;
    }

    /**
     * Shows all purchase orders placed by the current user (Buyer view).
     */
    @GetMapping("/my-purchases")
    public String showMyPurchases(Model model) {
        User user = authResolver.getCurrentUser();
        List<Order> purchases = orderService.findBuyerOrders(user.getId());
        model.addAttribute("orders", purchases);
        return "orders/purchases";
    }

    /**
     * Shows all sales orders containing books sold by the current user (Seller view).
     */
    @GetMapping("/my-sales")
    public String showMySales(Model model) {
        User user = authResolver.getCurrentUser();
        List<Order> sales = orderService.findSellerOrders(user.getId());
        model.addAttribute("orders", sales);
        return "orders/sales";
    }

    /**
     * Shows full order details. Enforces buyer or seller authorization.
     */
    @GetMapping("/{id}")
    public String showOrderDetail(@PathVariable Long id, Model model) {
        User user = authResolver.getCurrentUser();
        Order order = orderService.getOrderDetails(id, user);

        boolean isBuyer = order.isBuyer(user.getId());
        boolean isSeller = order.hasSeller(user.getId());

        model.addAttribute("order", order);
        model.addAttribute("isBuyer", isBuyer);
        model.addAttribute("isSeller", isSeller);
        model.addAttribute("currentUser", user);

        // If completed and current user is the buyer, check review status for each seller
        if (order.getStatus() == OrderStatus.COMPLETED && isBuyer) {
            Map<Long, Boolean> reviewStatusMap = new HashMap<>();
            for (OrderItem item : order.getItems()) {
                if (item.getSeller() != null) {
                    Long sellerId = item.getSeller().getId();
                    reviewStatusMap.put(sellerId, reviewService.hasReviewed(order.getId(), sellerId));
                }
            }
            model.addAttribute("reviewStatusMap", reviewStatusMap);
            model.addAttribute("reviewForm", new ReviewForm());
        }

        return "orders/detail";
    }

    /**
     * Seller confirms a pending order.
     */
    @PostMapping("/{id}/confirm")
    public String confirmOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        try {
            orderService.confirmOrder(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Order confirmed successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    /**
     * Seller completes a confirmed order.
     */
    @PostMapping("/{id}/complete")
    public String completeOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        try {
            orderService.completeOrder(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Order marked as COMPLETED! Transaction finalized.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    /**
     * Buyer confirms receipt of book. Triggers conditional payout disbursement.
     */
    @PostMapping("/{id}/confirm-received")
    public String confirmReceived(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        try {
            orderService.buyerConfirmReceipt(id, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Bạn đã xác nhận nhận sách thành công! Giao dịch hoàn tất và tiền thanh toán đã được giải ngân cho người bán.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    /**
     * Buyer opens a dispute against this order.
     */
    @PostMapping("/{id}/dispute")
    public String openDispute(@PathVariable Long id,
                              @org.springframework.web.bind.annotation.RequestParam("reason") String reason,
                              @org.springframework.web.bind.annotation.RequestParam(value = "description", required = false) String description,
                              @org.springframework.web.bind.annotation.RequestParam(value = "evidenceUrl", required = false) String evidenceUrl,
                              RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        try {
            disputeRefundService.openDispute(id, user, reason, description, evidenceUrl);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã mở khiếu nại thành công! Tiền thanh toán đã được khóa tạm giữ an toàn để Quản trị viên xử lý.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    /**
     * Cancels an order (by buyer or seller) and restores book listings.
     */
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        try {
            orderService.cancelOrder(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Order has been cancelled and book listings have been restored.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}
