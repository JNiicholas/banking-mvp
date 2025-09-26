package com.example.banking.service;

import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.entity.AccountEntity;
import com.example.banking.exception.BadRequestException;
import com.example.banking.exception.NotFoundException;
import com.example.banking.mapper.AccountEntityMapper;
import com.example.banking.mapper.CustomerEntityMapper;
import com.example.banking.mapper.TransactionEntityMapper;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.model.Transaction;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final AccountEntityMapper accountEntityMapper;
    private final CustomerEntityMapper customerEntityMapper;
    private final TransactionEntityMapper transactionEntityMapper;


    public Account createAccount(CreateAccountRequest req) {
        Customer c = customerRepository.findById(req.customerId())
                .map(customerEntityMapper::toDomain)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + req.customerId()));
        Account a = new Account(UUID.randomUUID(), c.getId());
        var saved = accountRepository.save(accountEntityMapper.toEntity(a));
        return accountEntityMapper.toDomain(saved);
    }

    public Account getAccount(UUID id) {
        return accountRepository.findById(id)
                .map(accountEntityMapper::toDomain)
                .orElseThrow(() -> new NotFoundException("Account not found: " + id));
    }

    private Account getAccountForUpdate(UUID id) {
        return accountRepository.findByIdForUpdate(id)
                .map(accountEntityMapper::toDomain)
                .orElseThrow(() -> new NotFoundException("Account not found: " + id));
    }

    @Transactional
    public Account deposit(UUID accountId, BigDecimal amount) {
        validateAmount(amount);

        AccountEntity entity = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        BigDecimal newBal = entity.getBalance().add(amount);
        entity.setBalance(newBal);
        accountRepository.save(entity);

        Transaction tx = new Transaction(UUID.randomUUID(), accountId, Instant.now(), Transaction.Type.DEPOSIT, amount, newBal);
        transactionRepository.save(transactionEntityMapper.toEntity(tx));

        return accountEntityMapper.toDomain(entity);
    }

    @Transactional
    public Account withdraw(UUID accountId, BigDecimal amount) {
        validateAmount(amount);

        AccountEntity entity = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        if (entity.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient funds");
        }

        BigDecimal newBal = entity.getBalance().subtract(amount);
        entity.setBalance(newBal);
        accountRepository.save(entity);

        Transaction tx = new Transaction(UUID.randomUUID(), accountId, Instant.now(), Transaction.Type.WITHDRAW, amount, newBal);
        transactionRepository.save(transactionEntityMapper.toEntity(tx));

        return accountEntityMapper.toDomain(entity);
    }

    public BigDecimal getBalance(UUID accountId) {
        return getAccount(accountId).getBalance();
    }

    public List<Transaction> getLastTransactions(UUID accountId, int limit) {
        int n = Math.max(1, limit);
        var page = PageRequest.of(0, n);
        return transactionRepository.findRecentByAccountId(accountId, page)
                .stream()
                .map(transactionEntityMapper::toDomain)
                .toList();
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
    }
}
