package com.banking.notification.service;

import com.banking.notification.entity.AuditLog;
import com.banking.notification.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLog record(String eventType, String entityType, String entityId, String accountNumber, String details) {
        AuditLog log = AuditLog.builder()
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .accountNumber(accountNumber)
                .details(details)
                .createdAt(Instant.now())
                .build();
        return auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getByAccount(String accountNumber) {
        return auditLogRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getByAccountAndDateRange(String accountNumber, Instant from, Instant to) {
        return auditLogRepository.findByAccountNumberAndCreatedAtBetweenOrderByCreatedAtDesc(
                accountNumber, from, to);
    }
}
