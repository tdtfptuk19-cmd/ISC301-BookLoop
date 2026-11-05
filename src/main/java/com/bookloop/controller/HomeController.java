package com.bookloop.controller;

import com.bookloop.entity.Product;
import com.bookloop.service.ProductService;
import com.bookloop.service.TextbookCatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller for the BookLoop homepage.
 */
@Controller
public class HomeController {

    private final ProductService productService;
    private final TextbookCatalogService catalogService;
    private final com.bookloop.service.CategoryService categoryService;

    public HomeController(ProductService productService,
                          TextbookCatalogService catalogService,
                          com.bookloop.service.CategoryService categoryService) {
        this.productService = productService;
        this.catalogService = catalogService;
        this.categoryService = categoryService;
    }

    /**
     * Renders the homepage with stats, promoted listings, recent listings, and university catalog shortcuts.
     */
    @GetMapping("/")
    public String home(Model model) {
        List<Product> recentListings = productService.findRecentAvailable();
        List<Product> promotedListings = productService.findPromotedAvailable();
        long totalListings = productService.countAvailable();

        model.addAttribute("recentListings", recentListings);
        model.addAttribute("promotedListings", promotedListings);
        model.addAttribute("totalListings", totalListings);
        model.addAttribute("institutions", catalogService.getActiveInstitutions());
        model.addAttribute("categories", categoryService.findAllActive());

        return "home";
    }
}
