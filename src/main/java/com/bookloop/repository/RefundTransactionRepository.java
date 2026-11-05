package com.bookloop.repository;

import com.bookloop.entity.RefundStatus;
import com.bookloop.entity.RefundTransaction;
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
public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, Long> {

    Optional<RefundTransaction> findByIdempotencyKey(String idempotencyKey);

    List<RefundTransaction> findByOrderId(Long orderId);

    List<RefundTransaction> findByPaymentTransactionId(Long paymentTransactionId);

    long countByStatus(RefundStatus status);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM RefundTransaction r WHERE r.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") RefundStatus status);

    @Query(value = "SELECT r FROM RefundTransaction r LEFT JOIN FETCH r.order LEFT JOIN FETCH r.paymentTransaction WHERE (:status IS NULL OR r.status = :status) ORDER BY r.createdAt DESC",
           countQuery = "SELECT COUNT(r) FROM RefundTransaction r WHERE (:status IS NULL OR r.status = :status)")
    Page<RefundTransaction> findAllWithDetails(@Param("status") RefundStatus status, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(r.refundAmount), 0) FROM RefundTransaction r
            WHERE r.status IN (:statuses)
              AND (:from IS NULL OR r.createdAt >= :from)
              AND (:to IS NULL OR r.createdAt <= :to)
            """)
    BigDecimal sumAmountByStatusesAndDateRange(@Param("statuses") List<RefundStatus> statuses,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(r) FROM RefundTransaction r
            WHERE r.status IN (:statuses)
              AND (:from IS NULL OR r.createdAt >= :from)
              AND (:to IS NULL OR r.createdAt <= :to)
            """)
    long countByStatusesAndDateRange(@Param("statuses") List<RefundStatus> statuses,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);
}
