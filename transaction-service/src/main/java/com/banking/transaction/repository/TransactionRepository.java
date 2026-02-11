package com.banking.transaction.repository;

import com.banking.transaction.entity.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByFromAccountNumberOrToAccountNumberOrderByCreatedAtDesc(
            String fromAccountNumber, String toAccountNumber);

    List<Transaction> findByFromAccountNumberOrToAccountNumberAndCreatedAtBetweenOrderByCreatedAtDesc(
            String from, String to, Instant start, Instant end);
}
