package com.bookloop.repository;

import com.bookloop.entity.CommissionRecord;
import com.bookloop.entity.CommissionStatus;
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
public interface CommissionRecordRepository extends JpaRepository<CommissionRecord, Long> {

    Optional<CommissionRecord> findByOrderId(Long orderId);

    List<CommissionRecord> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    @Query(value = "SELECT c FROM CommissionRecord c LEFT JOIN FETCH c.order WHERE c.seller.id = :sellerId ORDER BY c.createdAt DESC",
           countQuery = "SELECT COUNT(c) FROM CommissionRecord c WHERE c.seller.id = :sellerId")
    Page<CommissionRecord> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM CommissionRecord c WHERE c.status = :status")
    BigDecimal sumCommissionAmountByStatus(@Param("status") CommissionStatus status);

    @Query("SELECT COALESCE(SUM(c.sellerNetAmount), 0) FROM CommissionRecord c WHERE c.seller.id = :sellerId AND c.status = :status")
    BigDecimal sumSellerNetAmountByStatus(@Param("sellerId") Long sellerId, @Param("status") CommissionStatus status);

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM CommissionRecord c WHERE c.seller.id = :sellerId AND c.status = :status")
    BigDecimal sumSellerCommissionByStatus(@Param("sellerId") Long sellerId, @Param("status") CommissionStatus status);

    @Query(value = "SELECT c FROM CommissionRecord c LEFT JOIN FETCH c.order LEFT JOIN FETCH c.seller WHERE (:status IS NULL OR c.status = :status) ORDER BY c.createdAt DESC",
           countQuery = "SELECT COUNT(c) FROM CommissionRecord c WHERE (:status IS NULL OR c.status = :status)")
    Page<CommissionRecord> findAllWithDetails(@Param("status") CommissionStatus status, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(c.commissionAmount), 0) FROM CommissionRecord c
            WHERE c.status = :status
              AND (:from IS NULL OR c.createdAt >= :from)
              AND (:to IS NULL OR c.createdAt <= :to)
            """)
    BigDecimal sumCommissionAmountByStatusAndDateRange(@Param("status") CommissionStatus status,
                                                        @Param("from") LocalDateTime from,
                                                        @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(c) FROM CommissionRecord c
            WHERE c.status = :status
              AND (:from IS NULL OR c.createdAt >= :from)
              AND (:to IS NULL OR c.createdAt <= :to)
            """)
    long countByStatusAndDateRange(@Param("status") CommissionStatus status,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);
}
