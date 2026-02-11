package com.banking.account.dto;

import com.banking.account.entity.Account.AccountType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private AccountType type;

    @DecimalMin(value = "0", message = "Initial balance must be >= 0")
    @Builder.Default
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Size(min = 3, max = 3)
    @Builder.Default
    private String currency = "USD";
}
