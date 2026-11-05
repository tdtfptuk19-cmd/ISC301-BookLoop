package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.entity.Category;
import com.bookloop.entity.Condition;
import com.bookloop.entity.Product;
import com.bookloop.entity.User;
import com.bookloop.exception.ListingNotFoundException;
import com.bookloop.service.CategoryService;
import com.bookloop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public-facing controller for browsing and searching book listings.
 */
@Controller
@RequestMapping("/books")
public class BookController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AuthenticatedUserResolver authResolver;

    public BookController(ProductService productService,
                          CategoryService categoryService,
                          AuthenticatedUserResolver authResolver) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.authResolver = authResolver;
    }

    /**
     * Lists all available books with optional search filters.
     * All parameters are optional and can be combined freely.
     */
    @GetMapping
    public String listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String campus,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        // Support 'query' as alias for 'keyword' (e.g. from hero search)
        if ((keyword == null || keyword.trim().isEmpty()) && (query != null && !query.trim().isEmpty())) {
            keyword = query.trim();
        }

        // If categoryId is provided and category string is empty, resolve name
        if (categoryId != null && (category == null || category.trim().isEmpty())) {
            categoryService.findById(categoryId).ifPresent(c -> model.addAttribute("selectedCategoryId", c.getId()));
            category = categoryService.findById(categoryId).map(com.bookloop.entity.Category::getName).orElse(null);
        }

        // Validate and adjust minPrice / maxPrice range if inverted
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            minPrice = BigDecimal.ZERO;
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            maxPrice = null;
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            BigDecimal temp = minPrice;
            minPrice = maxPrice;
            maxPrice = temp;
            model.addAttribute("priceRangeWarning", "Khoảng giá đã được tự động điều chỉnh theo thứ tự từ nhỏ đến lớn.");
        }

        // Convert condition string to enum safely
        Condition conditionEnum = null;
        if (condition != null && !condition.trim().isEmpty()) {
            try {
                // If comma-separated due to browser param duplication, pick first
                String condVal = condition.contains(",") ? condition.split(",")[0].trim() : condition.trim();
                conditionEnum = Condition.valueOf(condVal.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Invalid condition value — treat as no filter
            }
        }

        Page<Product> productPage = productService.search(
                keyword, category, campus, conditionEnum, minPrice, maxPrice, sort, page, size);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("campuses", productService.findAvailableCampuses());
        model.addAttribute("conditions", Condition.values());

        // Preserve filter values so the UI can show them as selected
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category != null && category.contains(",") ? category.split(",")[0].trim() : category);
        model.addAttribute("selectedCampus", campus != null && campus.contains(",") ? campus.split(",")[0].trim() : campus);
        model.addAttribute("selectedCondition", conditionEnum != null ? conditionEnum.name() : null);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedSort", sort);

        return "books/list";
    }

    /**
     * Shows full details for a single listing.
     */
    @GetMapping("/{id}")
    public String showBook(@PathVariable Long id, Model model) {
        try {
            Product product = productService.findById(id);

            // If the authenticated user owns this listing, allow edit/manage links
            boolean isOwner = false;
            if (authResolver.isAuthenticated()) {
                try {
                    User currentUser = authResolver.getCurrentUser();
                    isOwner = product.getSeller() != null && product.getSeller().getId().equals(currentUser.getId());
                } catch (Exception ignored) {
                }
            }

            List<Product> relatedBooks = productService.findRelatedBooks(product, 4);

            model.addAttribute("product", product);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("relatedBooks", relatedBooks);
            return "books/detail";

        } catch (ListingNotFoundException e) {
            return "redirect:/books?notFound=true";
        }
    }
}
