package com.banking.transaction.dto;

import com.banking.transaction.entity.Transaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private String id;
    private Transaction.TransactionType type;
    private BigDecimal amount;
    private String fromAccountNumber;
    private String toAccountNumber;
    private Transaction.TransactionStatus status;
    private String reference;
    private Instant createdAt;

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .type(t.getType())
                .amount(t.getAmount())
                .fromAccountNumber(t.getFromAccountNumber())
                .toAccountNumber(t.getToAccountNumber())
                .status(t.getStatus())
                .reference(t.getReference())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
