package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.dto.ReportForm;
import com.bookloop.entity.Product;
import com.bookloop.entity.User;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.repository.ProductRepository;
import com.bookloop.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles user-facing report submission for listings.
 */
@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ProductRepository productRepository;
    private final AuthenticatedUserResolver userResolver;

    public ReportController(ReportService reportService,
                            ProductRepository productRepository,
                            AuthenticatedUserResolver userResolver) {
        this.reportService = reportService;
        this.productRepository = productRepository;
        this.userResolver = userResolver;
    }

    /**
     * Show the report form for a specific listing.
     */
    @GetMapping("/new")
    public String showReportForm(@RequestParam(required = false) Long productId, Model model) {
        User currentUser = userResolver.getCurrentUser();

        ReportForm form = new ReportForm();
        form.setProductId(productId);
        model.addAttribute("reportForm", form);
        model.addAttribute("cannotReport", false);

        if (productId != null) {
            productRepository.findById(productId).ifPresent(p -> {
                model.addAttribute("product", p);
                // Prevent seller from reporting their own listing
                if (p.getSeller().getId().equals(currentUser.getId())) {
                    model.addAttribute("cannotReport", true);
                }
            });
        }

        return "reports/new";
    }

    /**
     * Submit a report.
     */
    @PostMapping
    public String submitReport(@Valid @ModelAttribute("reportForm") ReportForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        User currentUser = userResolver.getCurrentUser();
        model.addAttribute("cannotReport", false);

        if (bindingResult.hasErrors()) {
            if (form.getProductId() != null) {
                productRepository.findById(form.getProductId())
                        .ifPresent(p -> {
                            model.addAttribute("product", p);
                            if (p.getSeller().getId().equals(currentUser.getId())) {
                                model.addAttribute("cannotReport", true);
                            }
                        });
            }
            return "reports/new";
        }

        try {
            reportService.submitReport(form, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Your report has been submitted. Thank you for helping keep BookLoop safe.");
            if (form.getProductId() != null) {
                return "redirect:/books/" + form.getProductId();
            }
            return "redirect:/";
        } catch (UnauthorizedOperationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("cannotReport", true);
            if (form.getProductId() != null) {
                productRepository.findById(form.getProductId())
                        .ifPresent(p -> model.addAttribute("product", p));
            }
            return "reports/new";
        }
    }
}

