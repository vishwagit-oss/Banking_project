package com.banking.transaction.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    private String id;

    private TransactionType type;

    private BigDecimal amount;

    private String fromAccountNumber;

    private String toAccountNumber;

    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    private String reference;

    private Instant createdAt;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER
    }

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED
    }
}
