package com.banking.account.service;

import com.banking.account.client.AuditClient;
import com.banking.account.dto.AccountRequest;
import com.banking.account.dto.AccountResponse;
import com.banking.account.entity.Account;
import com.banking.account.entity.Customer;
import com.banking.account.repository.AccountRepository;
import com.banking.account.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AuditClient auditClient;

    @Transactional(readOnly = true)
    public Optional<AccountResponse> getById(Long id) {
        return accountRepository.findById(id).map(AccountResponse::from);
    }

    @Transactional(readOnly = true)
    public Optional<AccountResponse> getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber).map(AccountResponse::from);
    }

    @Transactional(readOnly = true)
    public Optional<Account> getAccountEntityByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId).stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean validateAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .filter(a -> a.getStatus() == Account.AccountStatus.ACTIVE)
                .isPresent();
    }

    @Transactional
    public AccountResponse create(AccountRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.getCustomerId()));
        if (request.getType() == null) {
            throw new IllegalArgumentException("Account type is required (SAVINGS or CURRENT).");
        }
        if (accountRepository.existsByCustomerIdAndType(request.getCustomerId(), request.getType())) {
            throw new IllegalArgumentException(
                    "Customer already has a " + request.getType() + " account. Only one account per type allowed (one SAVINGS, one CURRENT).");
        }
        String accountNumber = generateAccountNumber();
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .type(request.getType())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : java.math.BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .customerId(customer.getId())
                .build();
        account = accountRepository.save(account);
        return AccountResponse.from(account);
    }

    /** Debit account (subtract amount). Used by Transaction Service. */
    @Transactional
    public void debit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active: " + accountNumber);
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        auditClient.record("DEBIT", "ACCOUNT", accountNumber, accountNumber, "amount=" + amount);
    }

    /** Credit account (add amount). Used by Transaction Service. */
    @Transactional
    public void credit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active: " + accountNumber);
        }
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        auditClient.record("CREDIT", "ACCOUNT", accountNumber, accountNumber, "amount=" + amount);
    }

    private String generateAccountNumber() {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "ACC" + unique;
    }
}
