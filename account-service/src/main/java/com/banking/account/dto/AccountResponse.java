package com.banking.account.dto;

import com.banking.account.entity.Account;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private Account.AccountType type;
    private BigDecimal balance;
    private Account.AccountStatus status;
    private String currency;
    private Long customerId;
    private Instant createdAt;

    public static AccountResponse from(Account account) {
        String number = account.getAccountNumber();
        if (number == null || number.isBlank()) {
            number = account.getId() != null ? "ACC" + account.getId() : "";
        }
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(number)
                .type(account.getType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .currency(account.getCurrency())
                .customerId(account.getCustomerId())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
