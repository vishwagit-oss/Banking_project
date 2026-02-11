package com.banking.account.web;

import com.banking.account.config.JwtPrincipal;
import com.banking.account.dto.CustomerRequest;
import com.banking.account.dto.CustomerResponse;
import com.banking.account.dto.ErrorResponse;
import com.banking.account.service.AccountService;
import com.banking.account.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerRestController {

    private final CustomerService customerService;
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<?> findAll(@AuthenticationPrincipal JwtPrincipal principal) {
        if (principal == null || !principal.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(customerService.findAll());
    }

    @GetMapping("/{customerId}/accounts")
    public ResponseEntity<?> getAccountsByCustomer(@PathVariable Long customerId,
                                                   @AuthenticationPrincipal JwtPrincipal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (principal.isCustomer() && !customerId.equals(principal.getCustomerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CustomerRequest request,
                                    @AuthenticationPrincipal JwtPrincipal principal) {
        if (principal == null || !principal.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            CustomerResponse created = customerService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (DuplicateKeyException e) {
            String msg = e.getMessage() != null && e.getMessage().contains("email") 
                ? "A customer with this email already exists: " + request.getEmail()
                : "Unable to create customer (duplicate key). Try again or use a different email.";
            return ResponseEntity.badRequest().body(new ErrorResponse(msg));
        }
    }
}
