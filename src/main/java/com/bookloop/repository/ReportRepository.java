package com.bookloop.repository;

import com.bookloop.entity.Report;
import com.bookloop.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    long countByStatus(ReportStatus status);

    @Query("""
            SELECT r FROM Report r
            LEFT JOIN FETCH r.reporter
            LEFT JOIN FETCH r.reportedProduct
            LEFT JOIN FETCH r.reportedUser
            WHERE (:status IS NULL OR r.status = :status)
            ORDER BY r.createdAt DESC
            """)
    Page<Report> findAllWithFilters(@Param("status") ReportStatus status, Pageable pageable);

    @Query("SELECT r FROM Report r LEFT JOIN FETCH r.reporter LEFT JOIN FETCH r.reportedProduct LEFT JOIN FETCH r.reportedUser ORDER BY r.createdAt DESC")
    Page<Report> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM Report r LEFT JOIN FETCH r.reporter LEFT JOIN FETCH r.reportedProduct LEFT JOIN FETCH r.reportedUser LEFT JOIN FETCH r.handledBy WHERE r.id = :id")
    java.util.Optional<Report> findByIdWithDetails(@Param("id") Long id);
}

