package com.banking.account.repository;

import com.banking.account.entity.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends MongoRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomerId(Long customerId);
    boolean existsByAccountNumber(String accountNumber);

    /** Used to enforce: at most one account per type per customer. */
    boolean existsByCustomerIdAndType(Long customerId, Account.AccountType type);
}
