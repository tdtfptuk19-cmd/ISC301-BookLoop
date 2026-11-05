package com.bookloop.service;

import com.bookloop.entity.FinancialAuditLog;
import com.bookloop.repository.FinancialAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service to record immutable financial audit logs.
 * Uses REQUIRES_NEW propagation to ensure audit trail is preserved even if enclosing tx rolls back.
 */
@Service
public class FinancialAuditService {

    private final FinancialAuditLogRepository auditLogRepository;

    public FinancialAuditService(FinancialAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FinancialAuditLog log(Long actorId, String actorEmail, String action, String entityType,
                                 Long entityId, String referenceNumber, BigDecimal amount,
                                 String oldValue, String newValue, String notes, String ipAddress) {
        FinancialAuditLog auditLog = new FinancialAuditLog(
                actorId, actorEmail, action, entityType, entityId, referenceNumber,
                amount, oldValue, newValue, notes, ipAddress
        );
        return auditLogRepository.save(auditLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FinancialAuditLog logEvent(String action, String entityType, Long entityId,
                                      String actorEmail, String details, BigDecimal amount, String referenceNumber) {
        return log(null, actorEmail, action, entityType, entityId, referenceNumber, amount, null, null, details, null);
    }
}

