package com.banking.account.web;

import com.banking.account.config.JwtPrincipal;
import com.banking.account.dto.AccountRequest;
import com.banking.account.dto.AccountResponse;
import com.banking.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountRestController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, @AuthenticationPrincipal JwtPrincipal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return accountService.getById(id)
                .map(acc -> {
                    if (principal.isCustomer() && !acc.getCustomerId().equals(principal.getCustomerId())) {
                        return ResponseEntity.<AccountResponse>status(HttpStatus.FORBIDDEN).build();
                    }
                    return ResponseEntity.ok(acc);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<?> getByAccountNumber(@PathVariable String accountNumber,
                                                Authentication auth) {
        // Internal service (transaction-service) validate-only: return 200 if account exists and ACTIVE, no body
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_SERVICE".equals(a.getAuthority()))) {
            boolean valid = accountService.validateAccount(accountNumber);
            return valid ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        }
        JwtPrincipal principal = (auth != null && auth.getPrincipal() instanceof JwtPrincipal) ? (JwtPrincipal) auth.getPrincipal() : null;
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return accountService.getByAccountNumber(accountNumber)
                .map(acc -> {
                    if (principal.isCustomer() && !acc.getCustomerId().equals(principal.getCustomerId())) {
                        return ResponseEntity.<AccountResponse>status(HttpStatus.FORBIDDEN).build();
                    }
                    return ResponseEntity.ok(acc);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AccountRequest request,
                                    @AuthenticationPrincipal JwtPrincipal principal) {
        if (principal == null || !principal.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AccountResponse created = accountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/number/{accountNumber}/debit")
    public ResponseEntity<Void> debit(@PathVariable String accountNumber, @RequestParam BigDecimal amount) {
        accountService.debit(accountNumber, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/number/{accountNumber}/credit")
    public ResponseEntity<Void> credit(@PathVariable String accountNumber, @RequestParam BigDecimal amount) {
        accountService.credit(accountNumber, amount);
        return ResponseEntity.ok().build();
    }
}
