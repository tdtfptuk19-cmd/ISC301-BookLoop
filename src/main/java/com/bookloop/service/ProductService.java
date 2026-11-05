package com.bookloop.service;

import com.bookloop.dto.ProductForm;
import com.bookloop.entity.Condition;
import com.bookloop.entity.ListingStatus;
import com.bookloop.entity.Product;
import com.bookloop.entity.User;
import com.bookloop.exception.ListingNotFoundException;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all business logic for book listings.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final com.bookloop.repository.AcademicBookRepository academicBookRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository,
                          com.bookloop.repository.AcademicBookRepository academicBookRepository,
                          CategoryService categoryService) {
        this.productRepository = productRepository;
        this.academicBookRepository = academicBookRepository;
        this.categoryService = categoryService;
    }

    /**
     * Returns the 8 most recently posted available listings (for homepage).
     */
    @Transactional(readOnly = true)
    public List<Product> findRecentAvailable() {
        return productRepository.findTop8ByStatusOrderByCreatedAtDesc(ListingStatus.AVAILABLE);
    }

    /**
     * Returns currently active promoted listings for homepage highlight.
     */
    @Transactional(readOnly = true)
    public List<Product> findPromotedAvailable() {
        return productRepository.findActivePromoted(ListingStatus.AVAILABLE, java.time.LocalDateTime.now());
    }

    /**
     * Searches available listings with optional filters.
     * All filter parameters are optional — null values are ignored.
     */
    @Transactional(readOnly = true)
    public Page<Product> search(String keyword, String category, String campus,
                                 Condition condition, BigDecimal minPrice, BigDecimal maxPrice,
                                 int page, int size) {
        return search(keyword, category, campus, condition, minPrice, maxPrice, "newest", page, size);
    }

    /**
     * Searches available listings with optional filters and sorting.
     */
    @Transactional(readOnly = true)
    public Page<Product> search(String keyword, String category, String campus,
                                 Condition condition, BigDecimal minPrice, BigDecimal maxPrice,
                                 String sort, int page, int size) {
        String kw = sanitizeParam(keyword);
        String cat = sanitizeParam(category);
        String cam = sanitizeParam(campus);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), ListingStatus.AVAILABLE));

            if (kw != null && !kw.isEmpty()) {
                String term = "%" + kw.toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), term);
                Predicate authorLike = cb.like(cb.lower(root.get("author")), term);
                Predicate courseLike = cb.like(cb.lower(root.get("courseCode")), term);
                Predicate descLike = cb.like(cb.lower(root.get("description")), term);
                Predicate isbnLike = cb.like(cb.lower(root.get("isbn")), term);
                predicates.add(cb.or(titleLike, authorLike, courseLike, descLike, isbnLike));
            }

            if (cat != null && !cat.isEmpty()) {
                Long catId = null;
                try {
                    catId = Long.parseLong(cat);
                } catch (NumberFormatException ignored) {}

                if (catId != null) {
                    final Long resolvedId = catId;
                    java.util.Optional<com.bookloop.entity.Category> optCat = categoryService.findById(resolvedId);
                    String catName = optCat.map(com.bookloop.entity.Category::getName).orElse(null);
                    if (catName != null) {
                        predicates.add(cb.or(
                                cb.equal(root.get("categoryRef").get("id"), resolvedId),
                                cb.equal(cb.lower(root.get("category")), catName.toLowerCase())
                        ));
                    } else {
                        predicates.add(cb.equal(root.get("categoryRef").get("id"), resolvedId));
                    }
                } else {
                    java.util.Optional<com.bookloop.entity.Category> optCat = categoryService.findByName(cat);
                    if (optCat.isEmpty()) {
                        optCat = categoryService.findBySlug(cat);
                    }
                    if (optCat.isPresent()) {
                        com.bookloop.entity.Category c = optCat.get();
                        predicates.add(cb.or(
                                cb.equal(root.get("categoryRef").get("id"), c.getId()),
                                cb.equal(cb.lower(root.get("category")), c.getName().toLowerCase()),
                                cb.equal(cb.lower(root.get("category")), cat.toLowerCase())
                        ));
                    } else {
                        predicates.add(cb.equal(cb.lower(root.get("category")), cat.toLowerCase()));
                    }
                }
            }

            if (cam != null && !cam.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("campus")), "%" + cam.toLowerCase() + "%"));
            }

            if (condition != null) {
                predicates.add(cb.equal(root.get("condition"), condition));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sortOrder = resolveSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return productRepository.findAll(spec, pageable);
    }

    private Sort resolveSort(String sort) {
        if ("price_asc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Order.desc("promoted"), Sort.Order.asc("price"), Sort.Order.desc("createdAt"));
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Order.desc("promoted"), Sort.Order.desc("price"), Sort.Order.desc("createdAt"));
        } else if ("title_asc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Order.asc("title"));
        } else {
            return Sort.by(Sort.Order.desc("promoted"), Sort.Order.desc("createdAt"));
        }
    }

    private String sanitizeParam(String param) {
        if (param == null) return null;
        String trimmed = param.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.contains(",")) {
            for (String part : trimmed.split(",")) {
                if (!part.trim().isEmpty()) {
                    return part.trim();
                }
            }
            return null;
        }
        return trimmed;
    }

    /**
     * Retrieves a listing by ID. Throws if not found.
     */
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseGet(() -> productRepository.findById(id)
                        .orElseThrow(() -> new ListingNotFoundException("Listing not found: " + id)));

        // Safely verify and initialize academicBook; if it's a broken reference, clear it so view won't crash
        if (product.getAcademicBook() != null) {
            try {
                // Accessing title initializes the proxy safely inside @Transactional
                if (product.getAcademicBook().getTitle() == null) {
                    product.setAcademicBook(null);
                }
            } catch (Exception e) {
                product.setAcademicBook(null);
            }
        }

        return product;
    }

    /**
     * Finds up to `limit` related available books for the given product.
     * Falls back to general available listings if fewer matching listings exist.
     */
    @Transactional(readOnly = true)
    public List<Product> findRelatedBooks(Product product, int limit) {
        if (product == null || product.getId() == null) {
            return new ArrayList<>();
        }
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> related = new ArrayList<>(productRepository.findRelatedByCategory(
                ListingStatus.AVAILABLE, product.getId(), product.getCategory(), pageable));
        if (related.size() < limit) {
            List<Product> recent = productRepository.findTop8ByStatusOrderByCreatedAtDesc(ListingStatus.AVAILABLE);
            for (Product p : recent) {
                if (!p.getId().equals(product.getId()) && !related.contains(p)) {
                    related.add(p);
                    if (related.size() >= limit) {
                        break;
                    }
                }
            }
        }
        return related;
    }

    /**
     * Returns all listings belonging to a specific seller (including non-AVAILABLE).
     */
    @Transactional(readOnly = true)
    public List<Product> findBySellerId(Long sellerId) {
        return productRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    /**
     * Returns distinct categories present in AVAILABLE listings.
     */
    @Transactional(readOnly = true)
    public List<String> findAvailableCategories() {
        return productRepository.findDistinctCategoriesByStatus(ListingStatus.AVAILABLE);
    }

    /**
     * Returns distinct campuses present in AVAILABLE listings.
     */
    @Transactional(readOnly = true)
    public List<String> findAvailableCampuses() {
        return productRepository.findDistinctCampusesByStatus(ListingStatus.AVAILABLE);
    }

    /**
     * Returns the total count of available listings.
     */
    @Transactional(readOnly = true)
    public long countAvailable() {
        return productRepository.countByStatus(ListingStatus.AVAILABLE);
    }

    /**
     * Creates a new listing. The seller is set from the authenticated user — never from form input.
     */
    @Transactional
    public Product createListing(ProductForm form, User seller) {
        Product product = new Product();
        applyFormToProduct(form, product);
        product.setSeller(seller);
        product.setStatus(ListingStatus.AVAILABLE);
        return productRepository.save(product);
    }

    /**
     * Updates an existing listing. Verifies ownership before applying changes.
     *
     * @throws UnauthorizedOperationException if the authenticated user does not own the listing
     */
    @Transactional
    public Product updateListing(Long productId, ProductForm form, User authenticatedUser) {
        Product product = findById(productId);
        verifyOwnership(product, authenticatedUser);
        applyFormToProduct(form, product);
        return productRepository.save(product);
    }

    /**
     * Deletes a listing. Verifies ownership before deleting.
     *
     * @throws UnauthorizedOperationException if the authenticated user does not own the listing
     */
    @Transactional
    public void deleteListing(Long productId, User authenticatedUser) {
        Product product = findById(productId);
        verifyOwnership(product, authenticatedUser);
        productRepository.delete(product);
    }

    // --- Private helpers ---

    private void applyFormToProduct(ProductForm form, Product product) {
        product.setTitle(form.getTitle());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setCondition(form.getCondition());
        if (form.getCategoryId() != null) {
            categoryService.findById(form.getCategoryId()).ifPresent(cat -> {
                product.setCategoryRef(cat);
                product.setCategory(cat.getName());
            });
        } else if (form.getCategory() != null && !form.getCategory().isBlank()) {
            product.setCategory(form.getCategory().trim());
            com.bookloop.entity.Category cat = categoryService.getOrCreate(form.getCategory().trim(), 99);
            product.setCategoryRef(cat);
        } else {
            product.setCategory(null);
            product.setCategoryRef(null);
        }
        product.setAuthor(form.getAuthor());
        product.setPublisher(form.getPublisher());
        product.setEdition(form.getEdition());
        product.setIsbn(form.getIsbn());
        product.setCourseCode(form.getCourseCode());
        product.setImageUrl(form.getImageUrl());
        product.setDefectNotes(form.getDefectNotes());
        product.setCampus(form.getCampus());
        if (form.getStatus() != null) {
            product.setStatus(form.getStatus());
        }
        if (form.getAcademicBookId() != null) {
            academicBookRepository.findById(form.getAcademicBookId())
                    .ifPresent(product::setAcademicBook);
        } else {
            product.setAcademicBook(null);
        }
    }

    private void verifyOwnership(Product product, User authenticatedUser) {
        if (!product.getSeller().getId().equals(authenticatedUser.getId())) {
            throw new UnauthorizedOperationException(
                    "You are not authorized to modify this listing.");
        }
    }
}
