package com.bookloop.repository;

import com.bookloop.entity.FinancialAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialAuditLogRepository extends JpaRepository<FinancialAuditLog, Long> {

    List<FinancialAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    List<FinancialAuditLog> findByReferenceNumberOrderByCreatedAtDesc(String referenceNumber);

    Page<FinancialAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

