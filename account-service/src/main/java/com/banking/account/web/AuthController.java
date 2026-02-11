package com.banking.account.web;

import com.banking.account.client.AuditClient;
import com.banking.account.dto.ActivateRequest;
import com.banking.account.dto.AuthResponse;
import com.banking.account.dto.ErrorResponse;
import com.banking.account.dto.LoginRequest;
import com.banking.account.entity.BankUser;
import com.banking.account.repository.BankUserRepository;
import com.banking.account.repository.CustomerRepository;
import com.banking.account.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.banking.account.config.JwtPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final BankUserRepository bankUserRepository;
    private final CustomerRepository customerRepository;
    private final AuditClient auditClient;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request.getUsername(), request.getPassword());
            auditClient.record("LOGIN_SUCCESS", "USER", request.getUsername(), null, "User logged in");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            auditClient.record("LOGIN_FAILED", "USER", request.getUsername(), null, e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /** First-time activation (like "New to online banking? Activate now"). */
    @PostMapping("/activate")
    public ResponseEntity<?> activate(@Valid @RequestBody ActivateRequest request) {
        try {
            AuthResponse response = authService.activate(request);
            auditClient.record("ACTIVATION_SUCCESS", "USER", request.getUsername(), null, "Customer activated online banking");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /** Current user info (requires valid JWT). */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal JwtPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        BankUser user = bankUserRepository.findByUsername(principal.getUsername()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        String customerName = user.getCustomerId() != null
                ? customerRepository.findById(user.getCustomerId()).map(c -> c.getName()).orElse(null)
                : "Admin";
        AuthResponse response = AuthResponse.builder()
                .username(user.getUsername())
                .customerId(user.getCustomerId())
                .role(user.getRole().name())
                .customerName(customerName)
                .build();
        return ResponseEntity.ok(response);
    }
}
