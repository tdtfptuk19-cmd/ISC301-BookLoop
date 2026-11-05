package com.bookloop.repository;

import com.bookloop.entity.Dispute;
import com.bookloop.entity.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    List<Dispute> findByOrderId(Long orderId);

    Optional<Dispute> findTopByOrderIdOrderByCreatedAtDesc(Long orderId);

    boolean existsByOrderIdAndStatusIn(Long orderId, List<DisputeStatus> statuses);

    long countByStatus(DisputeStatus status);

    @Query(value = "SELECT d FROM Dispute d LEFT JOIN FETCH d.order LEFT JOIN FETCH d.openedBy WHERE (:status IS NULL OR d.status = :status) ORDER BY d.createdAt DESC",
           countQuery = "SELECT COUNT(d) FROM Dispute d WHERE (:status IS NULL OR d.status = :status)")
    Page<Dispute> findAllWithDetails(@Param("status") DisputeStatus status, Pageable pageable);

    @Query("""
            SELECT COUNT(d) FROM Dispute d
            WHERE (:statuses IS NULL OR d.status IN (:statuses))
              AND (:from IS NULL OR d.createdAt >= :from)
              AND (:to IS NULL OR d.createdAt <= :to)
            """)
    long countByStatusesAndDateRange(@Param("statuses") List<DisputeStatus> statuses,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);
}
