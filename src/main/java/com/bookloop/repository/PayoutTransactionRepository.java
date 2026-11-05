package com.bookloop.repository;

import com.bookloop.entity.PayoutStatus;
import com.bookloop.entity.PayoutTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayoutTransactionRepository extends JpaRepository<PayoutTransaction, Long> {

    Optional<PayoutTransaction> findByIdempotencyKey(String idempotencyKey);

    Optional<PayoutTransaction> findByProviderPayoutId(String providerPayoutId);

    List<PayoutTransaction> findByOrderId(Long orderId);

    List<PayoutTransaction> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    @Query(value = "SELECT p FROM PayoutTransaction p LEFT JOIN FETCH p.order WHERE p.seller.id = :sellerId ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM PayoutTransaction p WHERE p.seller.id = :sellerId")
    Page<PayoutTransaction> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    long countByStatus(PayoutStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PayoutTransaction p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PayoutStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PayoutTransaction p WHERE p.seller.id = :sellerId AND p.status = :status")
    BigDecimal sumSellerAmountByStatus(@Param("sellerId") Long sellerId, @Param("status") PayoutStatus status);

    @Query(value = "SELECT p FROM PayoutTransaction p LEFT JOIN FETCH p.order LEFT JOIN FETCH p.seller WHERE (:status IS NULL OR p.status = :status) ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM PayoutTransaction p WHERE (:status IS NULL OR p.status = :status)")
    Page<PayoutTransaction> findAllWithDetails(@Param("status") PayoutStatus status, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM PayoutTransaction p
            WHERE p.status IN (:statuses)
              AND (:from IS NULL OR p.createdAt >= :from)
              AND (:to IS NULL OR p.createdAt <= :to)
            """)
    BigDecimal sumAmountByStatusesAndDateRange(@Param("statuses") List<PayoutStatus> statuses,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(p) FROM PayoutTransaction p
            WHERE p.status IN (:statuses)
              AND (:from IS NULL OR p.createdAt >= :from)
              AND (:to IS NULL OR p.createdAt <= :to)
            """)
    long countByStatusesAndDateRange(@Param("statuses") List<PayoutStatus> statuses,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);
}
