package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.entity.Cart;
import com.bookloop.entity.ListingStatus;
import com.bookloop.entity.User;
import com.bookloop.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling user shopping cart operations.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final AuthenticatedUserResolver authResolver;

    public CartController(CartService cartService, AuthenticatedUserResolver authResolver) {
        this.cartService = cartService;
        this.authResolver = authResolver;
    }

    /**
     * Displays the current user's shopping cart.
     */
    @GetMapping
    public String viewCart(Model model) {
        User user = authResolver.getCurrentUser();
        Cart cart = cartService.getCart(user);

        // Check if any items in cart are no longer AVAILABLE
        boolean hasUnavailableItems = cart.getItems().stream()
                .anyMatch(item -> item.getProduct() == null
                        || item.getProduct().getStatus() != ListingStatus.AVAILABLE);

        model.addAttribute("cart", cart);
        model.addAttribute("totalAmount", cart.calculateTotal());
        model.addAttribute("hasUnavailableItems", hasUnavailableItems);
        return "cart/view";
    }

    /**
     * Adds a book listing to the cart (POST).
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            RedirectAttributes redirectAttributes) {
        return processAddToCart(productId, false, redirectAttributes);
    }

    /**
     * Supports GET /cart/add (e.g. after login redirect).
     */
    @GetMapping("/add")
    public String addToCartGet(@RequestParam("productId") Long productId,
                               RedirectAttributes redirectAttributes) {
        return processAddToCart(productId, false, redirectAttributes);
    }

    /**
     * Buy Now action (POST): Adds book to cart and immediately redirects to checkout.
     */
    @PostMapping("/buy-now")
    public String buyNow(@RequestParam("productId") Long productId,
                         RedirectAttributes redirectAttributes) {
        return processAddToCart(productId, true, redirectAttributes);
    }

    /**
     * Supports GET /cart/buy-now (e.g. after login redirect for guest Buy Now).
     */
    @GetMapping("/buy-now")
    public String buyNowGet(@RequestParam("productId") Long productId,
                            RedirectAttributes redirectAttributes) {
        return processAddToCart(productId, true, redirectAttributes);
    }

    private String processAddToCart(Long productId, boolean buyNow, RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        try {
            cartService.addToCart(user, productId);
            if (buyNow) {
                return "redirect:/checkout";
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm vào giỏ hàng");
            return "redirect:/cart";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/books/" + productId;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm vào giỏ hàng: " + ex.getMessage());
            return "redirect:/books/" + productId;
        }
    }

    /**
     * Removes an item from the cart.
     */
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("productId") Long productId,
                                 RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        cartService.removeFromCart(user, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng.");
        return "redirect:/cart";
    }

    /**
     * Clears all items from the cart.
     */
    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        cartService.clearCart(user);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sạch giỏ hàng.");
        return "redirect:/cart";
    }
}
