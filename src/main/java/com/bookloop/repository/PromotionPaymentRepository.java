package com.bookloop.repository;

import com.bookloop.entity.PromotionPayment;
import com.bookloop.entity.PromotionPaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionPaymentRepository extends JpaRepository<PromotionPayment, Long> {

    @Query("SELECT p FROM PromotionPayment p LEFT JOIN FETCH p.product LEFT JOIN FETCH p.seller WHERE p.seller.id = :sellerId ORDER BY p.createdAt DESC")
    List<PromotionPayment> findBySellerIdOrderByCreatedAtDesc(@Param("sellerId") Long sellerId);

    @Query("SELECT p FROM PromotionPayment p LEFT JOIN FETCH p.product LEFT JOIN FETCH p.seller WHERE p.product.id = :productId ORDER BY p.createdAt DESC")
    List<PromotionPayment> findByProductIdOrderByCreatedAtDesc(@Param("productId") Long productId);

    Optional<PromotionPayment> findTopByProductIdOrderByCreatedAtDesc(Long productId);

    Optional<PromotionPayment> findByTransactionReference(String transactionReference);

    long countByStatus(PromotionPaymentStatus status);

    @Query("""
            SELECT p FROM PromotionPayment p LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.product
            WHERE (:status IS NULL OR p.status = :status)
            ORDER BY p.createdAt DESC
            """)
    Page<PromotionPayment> findAllWithFilters(@Param("status") PromotionPaymentStatus status, Pageable pageable);
}

