package com.banking.notification.config;

import com.banking.notification.entity.AuditLog;
import com.banking.notification.entity.Notification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;

import java.time.Instant;

@Configuration
public class MongoAuditConfig {

    @Bean
    public BeforeConvertCallback<Notification> notificationCreatedAt() {
        return (entity, collection) -> {
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(Instant.now());
            }
            return entity;
        };
    }

    @Bean
    public BeforeConvertCallback<AuditLog> auditLogCreatedAt() {
        return (entity, collection) -> {
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(Instant.now());
            }
            return entity;
        };
    }
}
