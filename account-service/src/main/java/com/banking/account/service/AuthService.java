package com.banking.account.service;

import com.banking.account.dto.ActivateRequest;
import com.banking.account.dto.AuthResponse;
import com.banking.account.entity.BankUser;
import com.banking.account.entity.Customer;
import com.banking.account.config.JwtUtil;
import com.banking.account.repository.BankUserRepository;
import com.banking.account.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final BankUserRepository bankUserRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BankUser user = bankUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    @Transactional
    public AuthResponse login(String username, String password) {
        BankUser user = bankUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        // Bank-style lockout after too many failed attempts
        if (user.getLockedUntil() != null && Instant.now().isBefore(user.getLockedUntil())) {
            throw new IllegalArgumentException("Account temporarily locked due to too many failed attempts. Try again later.");
        }
        UserDetails details = User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
        if (!passwordEncoder.matches(password, details.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(Instant.now().plusSeconds(LOCKOUT_MINUTES * 60L));
            }
            bankUserRepository.save(user);
            throw new IllegalArgumentException("Invalid username or password");
        }
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        bankUserRepository.save(user);
        String customerName = user.getCustomerId() != null
                ? customerRepository.findById(user.getCustomerId()).map(Customer::getName).orElse(null)
                : null;
        String token = jwtUtil.generateToken(user.getUsername(), user.getCustomerId(), user.getRole().name());
        return AuthResponse.from(token, user, customerName);
    }

    /** First-time activation: customer sets username and password (like Scotiabank "Activate now"). */
    @Transactional
    public AuthResponse activate(ActivateRequest request) {
        if (bankUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        if (bankUserRepository.existsByCustomerId(request.getCustomerId())) {
            throw new IllegalArgumentException("This customer has already activated online banking");
        }
        BankUser user = BankUser.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .customerId(request.getCustomerId())
                .role(BankUser.Role.CUSTOMER)
                .build();
        user = bankUserRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername(), user.getCustomerId(), user.getRole().name());
        return AuthResponse.from(token, user, customer.getName());
    }
}
