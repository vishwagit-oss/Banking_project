package com.banking.transaction.service;

import com.banking.transaction.client.AccountServiceClient;
import com.banking.transaction.dto.TransferRequest;
import com.banking.transaction.dto.TransactionResponse;
import com.banking.transaction.entity.Transaction;
import com.banking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        if (!accountServiceClient.validateAccount(request.getFromAccountNumber())) {
            throw new IllegalArgumentException("Invalid or inactive from account: " + request.getFromAccountNumber());
        }
        if (!accountServiceClient.validateAccount(request.getToAccountNumber())) {
            throw new IllegalArgumentException("Invalid or inactive to account: " + request.getToAccountNumber());
        }
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new IllegalArgumentException("From and to account cannot be the same");
        }

        Transaction tx = Transaction.builder()
                .type(Transaction.TransactionType.TRANSFER)
                .amount(request.getAmount())
                .fromAccountNumber(request.getFromAccountNumber())
                .toAccountNumber(request.getToAccountNumber())
                .reference(request.getReference())
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        tx = transactionRepository.save(tx);

        try {
            accountServiceClient.debit(request.getFromAccountNumber(), request.getAmount());
            accountServiceClient.credit(request.getToAccountNumber(), request.getAmount());
            tx.setStatus(Transaction.TransactionStatus.COMPLETED);
        } catch (Exception e) {
            tx.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(tx);
            throw e;
        }
        transactionRepository.save(tx);
        return TransactionResponse.from(tx);
    }

    @Transactional
    public TransactionResponse deposit(String accountNumber, BigDecimal amount, String reference) {
        if (!accountServiceClient.validateAccount(accountNumber)) {
            throw new IllegalArgumentException("Invalid or inactive account: " + accountNumber);
        }
        Transaction tx = Transaction.builder()
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(amount)
                .toAccountNumber(accountNumber)
                .reference(reference)
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        tx = transactionRepository.save(tx);
        try {
            accountServiceClient.credit(accountNumber, amount);
            tx.setStatus(Transaction.TransactionStatus.COMPLETED);
        } catch (Exception e) {
            tx.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(tx);
            throw e;
        }
        transactionRepository.save(tx);
        return TransactionResponse.from(tx);
    }

    @Transactional
    public TransactionResponse withdraw(String accountNumber, BigDecimal amount, String reference) {
        if (!accountServiceClient.validateAccount(accountNumber)) {
            throw new IllegalArgumentException("Invalid or inactive account: " + accountNumber);
        }
        Transaction tx = Transaction.builder()
                .type(Transaction.TransactionType.WITHDRAWAL)
                .amount(amount)
                .fromAccountNumber(accountNumber)
                .reference(reference)
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        tx = transactionRepository.save(tx);
        try {
            accountServiceClient.debit(accountNumber, amount);
            tx.setStatus(Transaction.TransactionStatus.COMPLETED);
        } catch (Exception e) {
            tx.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(tx);
            throw e;
        }
        transactionRepository.save(tx);
        return TransactionResponse.from(tx);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getHistory(String accountNumber) {
        return transactionRepository
                .findByFromAccountNumberOrToAccountNumberOrderByCreatedAtDesc(accountNumber, accountNumber)
                .stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    /** Validate that a transfer can be performed (SOAP Inquire). */
    public void validateTransfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (!accountServiceClient.validateAccount(fromAccountNumber)) {
            throw new IllegalArgumentException("Invalid or inactive from account: " + fromAccountNumber);
        }
        if (!accountServiceClient.validateAccount(toAccountNumber)) {
            throw new IllegalArgumentException("Invalid or inactive to account: " + toAccountNumber);
        }
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("From and to account cannot be the same");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getStatement(String accountNumber, Instant from, Instant to) {
        return transactionRepository
                .findByFromAccountNumberOrToAccountNumberAndCreatedAtBetweenOrderByCreatedAtDesc(
                        accountNumber, accountNumber, from, to)
                .stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }
}
