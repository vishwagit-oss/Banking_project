package com.banking.account.repository;

import com.banking.account.entity.BankUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BankUserRepository extends MongoRepository<BankUser, Long> {

    Optional<BankUser> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<BankUser> findByCustomerId(Long customerId);
    boolean existsByCustomerId(Long customerId);
    List<BankUser> findByRole(BankUser.Role role);
}
