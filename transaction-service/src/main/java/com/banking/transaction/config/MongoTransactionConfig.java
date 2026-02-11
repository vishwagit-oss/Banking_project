package com.banking.transaction.config;

import com.banking.transaction.entity.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;

import java.time.Instant;

@Configuration
public class MongoTransactionConfig {

    @Bean
    public BeforeConvertCallback<Transaction> transactionCreatedAt() {
        return (entity, collection) -> {
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(Instant.now());
            }
            return entity;
        };
    }
}
