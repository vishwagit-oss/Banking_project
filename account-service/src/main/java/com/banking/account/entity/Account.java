package com.banking.account.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "accounts")
@CompoundIndex(name = "customer_type_unique", def = "{ 'customerId' : 1, 'type' : 1 }", unique = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String accountNumber;

    private AccountType type;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Builder.Default
    private String currency = "USD";

    private Long customerId;

    private Instant createdAt;

    public enum AccountType {
        SAVINGS, CURRENT
    }

    public enum AccountStatus {
        ACTIVE, BLOCKED, CLOSED
    }
}
