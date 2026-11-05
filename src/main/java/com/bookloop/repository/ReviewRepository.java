package com.bookloop.repository;

import com.bookloop.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.buyer LEFT JOIN FETCH r.order WHERE r.seller.id = :sellerId AND r.hidden = false ORDER BY r.createdAt DESC")
    List<Review> findBySellerIdOrderByCreatedAtDesc(@Param("sellerId") Long sellerId);

    Optional<Review> findByOrderIdAndSellerId(Long orderId, Long sellerId);

    boolean existsByOrderIdAndSellerId(Long orderId, Long sellerId);

    @Query("SELECT AVG(CAST(r.rating as double)) FROM Review r WHERE r.seller.id = :sellerId AND r.hidden = false")
    Double findAverageRatingBySellerId(@Param("sellerId") Long sellerId);

    long countBySellerId(Long sellerId);

    /**
     * Admin: paginated list of all reviews with optional filters.
     */
    @Query("""
            SELECT r FROM Review r LEFT JOIN FETCH r.buyer LEFT JOIN FETCH r.seller
            WHERE (:keyword IS NULL OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(r.buyer.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:rating IS NULL OR r.rating = :rating)
              AND (:hidden IS NULL OR r.hidden = :hidden)
            ORDER BY r.createdAt DESC
            """)
    Page<Review> searchReviews(
            @Param("keyword") String keyword,
            @Param("rating") Integer rating,
            @Param("hidden") Boolean hidden,
            Pageable pageable);

    List<Review> findTop5ByOrderByCreatedAtDesc();
}
