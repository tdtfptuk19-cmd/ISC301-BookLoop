package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.dto.SellerFinanceOverviewDto;
import com.bookloop.entity.User;
import com.bookloop.payment.dto.SellerOnboardingRequest;
import com.bookloop.service.OrderService;
import com.bookloop.service.SellerFinanceService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller providing seller finance dashboard, bank account onboarding,
 * and order fulfillment actions (accept, dispatch).
 */
@Controller
@RequestMapping("/seller")
public class SellerFinanceController {

    private final SellerFinanceService sellerFinanceService;
    private final OrderService orderService;
    private final AuthenticatedUserResolver authResolver;

    public SellerFinanceController(SellerFinanceService sellerFinanceService,
                                   OrderService orderService,
                                   AuthenticatedUserResolver authResolver) {
        this.sellerFinanceService = sellerFinanceService;
        this.orderService = orderService;
        this.authResolver = authResolver;
    }

    /**
     * Seller Finance Dashboard: shows revenue, pending escrow, payouts, and bank info.
     */
    @GetMapping("/finance")
    public String showFinanceDashboard(Model model) {
        User user = authResolver.getCurrentUser();
        SellerFinanceOverviewDto overview = sellerFinanceService.getFinanceOverview(user.getId());

        model.addAttribute("overview", overview);
        model.addAttribute("payouts", sellerFinanceService.getPayouts(user.getId(), PageRequest.of(0, 15)));
        model.addAttribute("commissions", sellerFinanceService.getCommissions(user.getId(), PageRequest.of(0, 15)));

        SellerOnboardingRequest form = new SellerOnboardingRequest();
        if (overview.getPaymentAccount() != null) {
            form.setBankCode(overview.getPaymentAccount().getBankCode());
            form.setBankName(overview.getPaymentAccount().getBankName());
            form.setAccountNumber(overview.getPaymentAccount().getAccountNumber());
            form.setAccountHolderName(overview.getPaymentAccount().getAccountHolderName());
            form.setCitizenId(overview.getPaymentAccount().getCitizenId());
        }
        model.addAttribute("onboardingForm", form);

        return "seller/finance";
    }

    /**
     * Connect or update seller payout bank account.
     */
    @PostMapping("/finance/account/onboard")
    public String onboardBankAccount(@ModelAttribute("onboardingForm") SellerOnboardingRequest form,
                                     RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        try {
            sellerFinanceService.onboardAccount(user, form);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã lưu và liên kết tài khoản ngân hàng nhận tiền thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi liên kết tài khoản ngân hàng: " + ex.getMessage());
        }
        return "redirect:/seller/finance";
    }

    /**
     * Triggers payout disbursement for any completed sales orders that haven't been disbursed yet.
     */
    @PostMapping("/finance/disburse-now")
    public String disburseCompletedOrders(RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        try {
            int count = sellerFinanceService.disburseUndisbursedOrders(user.getId(), user);
            if (count > 0) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã kích hoạt giải ngân thành công " + count + " đơn hàng về tài khoản ngân hàng của bạn!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Hiện không có đơn hàng nào cần giải ngân hoặc tất cả đơn đã được giải ngân.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi thực hiện giải ngân: " + ex.getMessage());
        }
        return "redirect:/seller/finance";
    }

    /**
     * Seller accepts an incoming order.
     */
    @PostMapping("/orders/{id}/accept")
    public String acceptOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        try {
            orderService.sellerAcceptOrder(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận chấp nhận đơn hàng #" + id + "!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    /**
     * Seller marks order as dispatched/shipped to student buyer.
     */
    @PostMapping("/orders/{id}/dispatch")
    public String dispatchOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        try {
            orderService.sellerDispatchOrder(id, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã cập nhật trạng thái: Đã gửi giáo trình tới người mua!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}

