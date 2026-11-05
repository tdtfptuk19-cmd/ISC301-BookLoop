package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.dto.ReviewForm;
import com.bookloop.entity.User;
import com.bookloop.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling buyer reviews for completed orders.
 */
@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthenticatedUserResolver authResolver;

    public ReviewController(ReviewService reviewService, AuthenticatedUserResolver authResolver) {
        this.reviewService = reviewService;
        this.authResolver = authResolver;
    }

    /**
     * Submits a rating and written review for a seller on a completed order.
     */
    @PostMapping
    public String submitReview(@Valid @ModelAttribute("reviewForm") ReviewForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        User buyer = authResolver.getCurrentUser();

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Invalid review submission. Please ensure rating is between 1 and 5 stars.");
            return "redirect:/orders/" + form.getOrderId();
        }

        try {
            reviewService.submitReview(
                    form.getOrderId(),
                    form.getSellerId(),
                    form.getRating(),
                    form.getComment(),
                    buyer
            );
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thank you! Your " + form.getRating() + "-star review has been published.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/orders/" + form.getOrderId();
    }
}
