package com.banking.account.config;

import com.banking.account.entity.Account;
import com.banking.account.entity.BankUser;
import com.banking.account.entity.Customer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;

import java.time.Instant;

@Configuration
public class MongoIdAndAuditConfig {

    @Bean
    public BeforeConvertCallback<Customer> customerIdGenerator(@Lazy MongoSequenceService sequence) {
        return (entity, collection) -> {
            if (entity.getId() == null) {
                entity.setId(sequence.nextSequence("customers"));
            }
            return entity;
        };
    }

    @Bean
    public BeforeConvertCallback<Account> accountIdAndCreatedAt(@Lazy MongoSequenceService sequence) {
        return (entity, collection) -> {
            if (entity.getId() == null) {
                entity.setId(sequence.nextSequence("accounts"));
            }
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(Instant.now());
            }
            return entity;
        };
    }

    @Bean
    public BeforeConvertCallback<BankUser> bankUserIdGenerator(@Lazy MongoSequenceService sequence) {
        return (entity, collection) -> {
            if (entity.getId() == null) {
                entity.setId(sequence.nextSequence("bank_users"));
            }
            return entity;
        };
    }
}
