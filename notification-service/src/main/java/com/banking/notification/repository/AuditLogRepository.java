package com.banking.notification.repository;

import com.banking.notification.entity.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);

    List<AuditLog> findByAccountNumberAndCreatedAtBetweenOrderByCreatedAtDesc(
            String accountNumber, Instant from, Instant to);
}
