package com.bookloop.controller;

import com.bookloop.config.AuthenticatedUserResolver;
import com.bookloop.dto.ProductForm;
import com.bookloop.entity.Condition;
import com.bookloop.entity.ListingStatus;
import com.bookloop.entity.Product;
import com.bookloop.entity.User;
import com.bookloop.exception.ListingNotFoundException;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for authenticated seller operations:
 * - My Listings
 * - Create listing
 * - Edit listing
 * - Delete listing
 *
 * All operations verify that the authenticated user owns the resource
 * before performing any modifications.
 */
@Controller
@RequestMapping("/my-listings")
public class ListingController {

    private final ProductService productService;
    private final AuthenticatedUserResolver authResolver;
    private final com.bookloop.service.PromotionService promotionService;
    private final com.bookloop.service.PlatformSettingService settingService;
    private final com.bookloop.repository.AcademicBookRepository academicBookRepository;
    private final com.bookloop.service.CategoryService categoryService;

    public ListingController(ProductService productService,
                             AuthenticatedUserResolver authResolver,
                             com.bookloop.service.PromotionService promotionService,
                             com.bookloop.service.PlatformSettingService settingService,
                             com.bookloop.repository.AcademicBookRepository academicBookRepository,
                             com.bookloop.service.CategoryService categoryService) {
        this.productService = productService;
        this.authResolver = authResolver;
        this.promotionService = promotionService;
        this.settingService = settingService;
        this.academicBookRepository = academicBookRepository;
        this.categoryService = categoryService;
    }

    // --- My Listings ---

    @GetMapping
    public String myListings(Model model) {
        User user = authResolver.getCurrentUser();
        List<Product> listings = productService.findBySellerId(user.getId());
        model.addAttribute("listings", listings);
        return "listings/my-listings";
    }

    // --- Create Listing ---

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(required = false) Long academicBookId, Model model) {
        ProductForm form = new ProductForm();
        if (academicBookId != null) {
            academicBookRepository.findById(academicBookId).ifPresent(ab -> {
                form.setTitle(ab.getTitle());
                form.setAuthor(ab.getAuthor());
                form.setIsbn(ab.getIsbn());
                form.setCategory(ab.getCategory());
                form.setImageUrl(ab.getCoverImageUrl());
                form.setAcademicBookId(ab.getId());
            });
        }
        model.addAttribute("productForm", form);
        model.addAttribute("conditions", Condition.values());
        model.addAttribute("statuses", ListingStatus.values());
        model.addAttribute("academicBooks", academicBookRepository.findAll());
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("promotionFee", settingService.getListingPromotionFee());
        model.addAttribute("durationDays", settingService.getPromotionDurationDays());
        model.addAttribute("isEdit", false);
        return "listings/form";
    }

    @PostMapping("/new")
    public String createListing(
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("conditions", Condition.values());
            model.addAttribute("statuses", ListingStatus.values());
            model.addAttribute("academicBooks", academicBookRepository.findAll());
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("promotionFee", settingService.getListingPromotionFee());
            model.addAttribute("durationDays", settingService.getPromotionDurationDays());
            model.addAttribute("isEdit", false);
            return "listings/form";
        }

        User user = authResolver.getCurrentUser();
        Product saved = productService.createListing(form, user);

        // Create initial promotion payment record
        com.bookloop.entity.PromotionPayment payment = promotionService.createPaymentForNewListing(saved, user);

        redirectAttributes.addFlashAttribute("successMessage",
                "Tin đăng \"" + saved.getTitle() + "\" đã được tạo thành công! Vui lòng hoàn tất thanh toán phí để được duyệt và hiển thị nổi bật.");
        return "redirect:/my-listings/" + saved.getId() + "/promotion-payment";
    }

    // --- Promotion Payment Page for a Listing ---

    @GetMapping("/{id}/promotion-payment")
    public String showPromotionPaymentPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        Product product = productService.findById(id);

        if (!product.getSeller().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập thanh toán cho tin đăng này.");
            return "redirect:/my-listings";
        }

        com.bookloop.entity.PromotionPayment payment = promotionService.getPaymentForListing(id);
        if (payment == null) {
            payment = promotionService.createPaymentForNewListing(product, user);
        }

        model.addAttribute("product", product);
        model.addAttribute("payment", payment);
        model.addAttribute("instructions", settingService.getPaymentInstructions());
        return "listings/promotion-payment";
    }

    @PostMapping("/{id}/promotion-payment")
    public String submitPromotionPayment(@PathVariable Long id,
                                         @RequestParam(required = false) String transactionReference,
                                         @RequestParam(required = false) String paymentNotes,
                                         RedirectAttributes redirectAttributes) {
        User user = authResolver.getCurrentUser();
        Product product = productService.findById(id);

        if (!product.getSeller().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thao tác trên tin đăng này.");
            return "redirect:/my-listings";
        }

        com.bookloop.entity.PromotionPayment payment = promotionService.getPaymentForListing(id);
        if (payment != null && payment.getStatus() == com.bookloop.entity.PromotionPaymentStatus.PENDING) {
            promotionService.submitPaymentProof(payment.getId(), transactionReference, paymentNotes, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã ghi nhận thông tin thanh toán! Quản trị viên sẽ kiểm tra và kích hoạt huy hiệu nổi bật trong thời gian sớm nhất.");
        }

        return "redirect:/my-listings/" + id + "/promotion-payment";
    }

    // --- Edit Listing ---

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id);
            User user = authResolver.getCurrentUser();

            // Verify ownership before showing edit form
            if (!product.getSeller().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "You are not authorized to edit this listing.");
                return "redirect:/my-listings";
            }

            // Pre-populate the form with current values
            ProductForm form = new ProductForm();
            form.setTitle(product.getTitle());
            form.setDescription(product.getDescription());
            form.setPrice(product.getPrice());
            form.setCondition(product.getCondition());
            form.setCategory(product.getCategory());
            form.setAuthor(product.getAuthor());
            form.setPublisher(product.getPublisher());
            form.setEdition(product.getEdition());
            form.setIsbn(product.getIsbn());
            form.setCourseCode(product.getCourseCode());
            form.setImageUrl(product.getImageUrl());
            form.setDefectNotes(product.getDefectNotes());
            form.setCampus(product.getCampus());
            form.setStatus(product.getStatus());
            if (product.getAcademicBook() != null) {
                form.setAcademicBookId(product.getAcademicBook().getId());
            }

            if (product.getCategoryRef() != null) {
                form.setCategoryId(product.getCategoryRef().getId());
                form.setCategory(product.getCategoryRef().getName());
            } else if (product.getCategory() != null) {
                form.setCategory(product.getCategory());
                categoryService.findByName(product.getCategory()).ifPresent(c -> form.setCategoryId(c.getId()));
            }

            model.addAttribute("productForm", form);
            model.addAttribute("productId", id);
            model.addAttribute("conditions", Condition.values());
            model.addAttribute("statuses", ListingStatus.values());
            model.addAttribute("academicBooks", academicBookRepository.findAll());
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("isEdit", true);
            return "listings/form";

        } catch (ListingNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Listing not found.");
            return "redirect:/my-listings";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateListing(
            @PathVariable Long id,
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("conditions", Condition.values());
            model.addAttribute("statuses", ListingStatus.values());
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("isEdit", true);
            return "listings/form";
        }

        try {
            User user = authResolver.getCurrentUser();
            Product updated = productService.updateListing(id, form, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Listing \"" + updated.getTitle() + "\" updated successfully!");
            return "redirect:/my-listings";

        } catch (ListingNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Listing not found.");
            return "redirect:/my-listings";
        } catch (UnauthorizedOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/my-listings";
        }
    }

    // --- Delete Listing ---

    /**
     * Shows a confirmation page before deletion.
     */
    @GetMapping("/{id}/delete")
    public String showDeleteConfirmation(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id);
            User user = authResolver.getCurrentUser();

            if (!product.getSeller().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "You are not authorized to delete this listing.");
                return "redirect:/my-listings";
            }

            model.addAttribute("product", product);
            return "listings/delete-confirm";

        } catch (ListingNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Listing not found.");
            return "redirect:/my-listings";
        }
    }

    /**
     * Processes confirmed deletion via POST.
     */
    @PostMapping("/{id}/delete")
    public String deleteListing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = authResolver.getCurrentUser();
            productService.deleteListing(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Listing deleted successfully.");

        } catch (ListingNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Listing not found.");
        } catch (UnauthorizedOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/my-listings";
    }

    // --- Seller's Promotion Payments History ---

    @GetMapping("/payments")
    public String myPromotions(Model model) {
        User user = authResolver.getCurrentUser();
        List<com.bookloop.entity.PromotionPayment> payments = promotionService.getSellerPayments(user);
        model.addAttribute("payments", payments);
        return "listings/payments";
    }
}
