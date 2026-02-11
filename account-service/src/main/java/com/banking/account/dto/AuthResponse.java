package com.banking.account.dto;

import com.banking.account.entity.BankUser;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String username;
    private Long customerId;   // null for admin
    private String role;
    private String customerName; // for display

    public static AuthResponse from(String token, BankUser user, String customerName) {
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .customerId(user.getCustomerId())
                .role(user.getRole().name())
                .customerName(customerName)
                .build();
    }
}
