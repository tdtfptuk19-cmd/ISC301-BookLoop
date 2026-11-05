package com.bookloop.repository;

import com.bookloop.entity.PaymentStatus;
import com.bookloop.entity.PaymentTransaction;
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
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByMerchantReference(String merchantReference);

    Optional<PaymentTransaction> findByProviderPaymentId(String providerPaymentId);

    List<PaymentTransaction> findByOrderId(Long orderId);

    Optional<PaymentTransaction> findTopByOrderIdOrderByCreatedAtDesc(Long orderId);

    long countByPaymentStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.paymentStatus = :status")
    BigDecimal sumAmountByPaymentStatus(@Param("status") PaymentStatus status);

    @Query(value = "SELECT p FROM PaymentTransaction p LEFT JOIN FETCH p.order WHERE (:status IS NULL OR p.paymentStatus = :status) ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM PaymentTransaction p WHERE (:status IS NULL OR p.paymentStatus = :status)")
    Page<PaymentTransaction> findAllWithOrder(@Param("status") PaymentStatus status, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p
            WHERE p.paymentStatus IN (:statuses)
              AND (:from IS NULL OR p.createdAt >= :from)
              AND (:to IS NULL OR p.createdAt <= :to)
            """)
    BigDecimal sumAmountByStatusesAndDateRange(@Param("statuses") List<PaymentStatus> statuses,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(p) FROM PaymentTransaction p
            WHERE p.paymentStatus IN (:statuses)
              AND (:from IS NULL OR p.createdAt >= :from)
              AND (:to IS NULL OR p.createdAt <= :to)
            """)
    long countByStatusesAndDateRange(@Param("statuses") List<PaymentStatus> statuses,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p
            WHERE p.paymentStatus = :status
              AND (:from IS NULL OR p.createdAt >= :from)
              AND (:to IS NULL OR p.createdAt <= :to)
            """)
    BigDecimal sumAmountByStatusAndDateRange(@Param("status") PaymentStatus status,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(p) FROM PaymentTransaction p
            WHERE p.paymentStatus = :status
              AND (:from IS NULL OR p.createdAt >= :from)
              AND (:to IS NULL OR p.createdAt <= :to)
            """)
    long countByStatusAndDateRange(@Param("status") PaymentStatus status,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);
}
