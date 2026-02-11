package com.banking.account.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "bank_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankUser {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String username;

    private String passwordHash;

    /** Customer ID for customer users; null for admin. */
    private Long customerId;

    private Role role;

    /** Failed login count; reset on success. Used for lockout. */
    @Builder.Default
    private int failedLoginAttempts = 0;

    /** If set, login blocked until this time (bank-style lockout after too many failures). */
    private Instant lockedUntil;

    public enum Role {
        CUSTOMER, ADMIN
    }
}
