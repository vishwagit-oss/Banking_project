package com.banking.account.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures MongoDB sequences are at least (max _id + 1) in each collection at startup.
 * This fixes the case where the first customer (or account, etc.) was created without the sequence (e.g. manual insert or seed).
 */
@Component
@Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class MongoSequenceInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoSequenceInitializer.class);

    private final MongoSequenceService sequenceService;
    private final MongoTemplate mongoTemplate;

    public MongoSequenceInitializer(MongoSequenceService sequenceService, MongoTemplate mongoTemplate) {
        this.sequenceService = sequenceService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        String dbName = mongoTemplate.getDb().getName();
        log.info("MongoDB database in use: {}", dbName);
        sequenceService.ensureSequenceAtLeast("customers", "customers");
        sequenceService.ensureSequenceAtLeast("accounts", "accounts");
        sequenceService.ensureSequenceAtLeast("bank_users", "bank_users");
    }
}
