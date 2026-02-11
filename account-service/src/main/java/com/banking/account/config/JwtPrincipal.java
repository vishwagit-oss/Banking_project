package com.banking.account.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class JwtPrincipal {
    private final String username;
    private final Long customerId;
    private final String role;

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isCustomer() {
        return "CUSTOMER".equals(role);
    }
}
