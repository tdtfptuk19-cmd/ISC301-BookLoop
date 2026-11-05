package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.dto.ProfileUpdateForm;
import com.bookloop.entity.User;
import com.bookloop.service.ProductService;
import com.bookloop.service.ReviewService;
import com.bookloop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for viewing and updating the authenticated user's profile,
 * as well as viewing public seller profiles with their reviews and ratings.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final ProductService productService;
    private final ReviewService reviewService;
    private final AuthenticatedUserResolver authResolver;

    public ProfileController(UserService userService,
                             ProductService productService,
                             ReviewService reviewService,
                             AuthenticatedUserResolver authResolver) {
        this.userService = userService;
        this.productService = productService;
        this.reviewService = reviewService;
        this.authResolver = authResolver;
    }

    /**
     * Shows the current user's profile page.
     */
    @GetMapping
    public String showProfile(Model model) {
        User user = authResolver.getCurrentUser();
        model.addAttribute("user", user);

        // Pre-populate the edit form with current values
        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setFullName(user.getFullName());
        form.setUniversity(user.getUniversity());
        form.setCampus(user.getCampus());
        form.setPhone(user.getPhone());
        form.setAvatarUrl(user.getAvatarUrl());
        model.addAttribute("profileForm", form);

        return "profile/view";
    }

    /**
     * Processes profile update form submission.
     */
    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileUpdateForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        User user = authResolver.getCurrentUser();

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "profile/view";
        }

        userService.updateProfile(user.getId(), form);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/profile";
    }

    /**
     * Shows a public seller profile page with active listings, average rating,
     * total review count, and buyer reviews.
     */
    @GetMapping("/seller/{sellerId}")
    public String showSellerProfile(@PathVariable Long sellerId, Model model) {
        User seller = userService.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        model.addAttribute("seller", seller);
        model.addAttribute("listings", productService.findBySellerId(sellerId));
        model.addAttribute("avgRating", reviewService.getAverageRatingForSeller(sellerId));
        model.addAttribute("reviewCount", reviewService.getReviewCountForSeller(sellerId));
        model.addAttribute("reviews", reviewService.getReviewsForSeller(sellerId));

        return "profile/seller";
    }
}
