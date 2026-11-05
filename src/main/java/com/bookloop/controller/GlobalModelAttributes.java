package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.entity.User;
import com.bookloop.service.CartService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Supplies global model attributes (such as the live cart item count)
 * across all controllers for rendering in the shared navbar.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final CartService cartService;
    private final AuthenticatedUserResolver authResolver;

    public GlobalModelAttributes(CartService cartService, AuthenticatedUserResolver authResolver) {
        this.cartService = cartService;
        this.authResolver = authResolver;
    }

    @ModelAttribute("cartItemCount")
    public int getCartItemCount() {
        if (authResolver.isAuthenticated()) {
            try {
                User user = authResolver.getCurrentUser();
                return cartService.getCartItemCount(user);
            } catch (Exception ignored) {
                return 0;
            }
        }
        return 0;
    }
}
