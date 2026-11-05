package com.bookloop.repository;

import com.bookloop.entity.Condition;
import com.bookloop.entity.ListingStatus;
import com.bookloop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Product entity. Uses JpaSpecificationExecutor for dynamic filtering.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    /**
     * Returns all AVAILABLE listings ordered by most recent first (for homepage).
     */
    List<Product> findTop8ByStatusOrderByCreatedAtDesc(ListingStatus status);

    /**
     * Returns all listings belonging to a seller (for My Listings page).
     */
    List<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    /**
     * Returns all AVAILABLE listings with combined filters (used in search).
     * All filter parameters are optional; null values are ignored.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
              AND (:campus IS NULL OR LOWER(p.campus) LIKE LOWER(CONCAT('%', :campus, '%')))
              AND (:condition IS NULL OR p.condition = :condition)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            ORDER BY p.promoted DESC, p.createdAt DESC
            """)
    Page<Product> searchAvailable(
            @Param("status") ListingStatus status,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("campus") String campus,
            @Param("condition") Condition condition,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * Active promoted listings for homepage highlight.
     */
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.promoted = true AND p.promotedUntil > :now ORDER BY p.createdAt DESC")
    List<Product> findActivePromoted(@Param("status") ListingStatus status, @Param("now") java.time.LocalDateTime now);

    /**
     * Finds used listings mapped to an academic catalog book.
     */
    List<Product> findByAcademicBookIdAndStatusOrderByCreatedAtDesc(Long academicBookId, ListingStatus status);

    /**
     * Returns distinct categories from all available listings (for filter dropdown).
     */
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.status = :status AND p.category IS NOT NULL ORDER BY p.category")
    List<String> findDistinctCategoriesByStatus(@Param("status") ListingStatus status);

    /**
     * Returns distinct campuses from all available listings (for filter dropdown).
     */
    @Query("SELECT DISTINCT p.campus FROM Product p WHERE p.status = :status ORDER BY p.campus")
    List<String> findDistinctCampusesByStatus(@Param("status") ListingStatus status);

    /**
     * Finds related available books in the same category or with the same course code,
     * excluding the specified product ID.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND p.id != :excludeId
              AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
            ORDER BY p.promoted DESC, p.createdAt DESC
            """)
    List<Product> findRelatedByCategory(
            @Param("status") ListingStatus status,
            @Param("excludeId") Long excludeId,
            @Param("category") String category,
            Pageable pageable
    );

    /**
     * Count of available listings.
     */
    long countByStatus(ListingStatus status);

    /**
     * Retrieves a listing by ID eagerly fetching its seller and academic book
     * to safely render detail views without LazyInitializationException.
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.academicBook WHERE p.id = :id")
    java.util.Optional<Product> findByIdWithDetails(@Param("id") Long id);

    /**
     * Admin: paginated list of all listings (any status) with optional keyword, status, and category filter.
     */
    @Query(value = """
            SELECT p FROM Product p LEFT JOIN FETCH p.seller
            WHERE (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.author) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR p.status = :status)
              AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
            ORDER BY p.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(p) FROM Product p
            WHERE (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.author) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR p.status = :status)
              AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
            """)
    Page<Product> searchAllForAdmin(
            @Param("keyword") String keyword,
            @Param("status") ListingStatus status,
            @Param("category") String category,
            Pageable pageable);

    /**
     * Recent listings for dashboard.
     */
    List<Product> findTop5ByOrderByCreatedAtDesc();

    /**
     * Returns all listings with the given status ordered by most recent first.
     * Used in BugFixesIntegrationTest to verify realistic VND pricing.
     */
    List<Product> findByStatusOrderByCreatedAtDesc(ListingStatus status);
}
