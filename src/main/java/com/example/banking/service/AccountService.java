
package com.example.banking.service;

import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.exception.BadRequestException;
import com.example.banking.exception.NotFoundException;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.model.Transaction;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.mapper.AccountEntityMapper;
import com.example.banking.mapper.CustomerEntityMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountEntityMapper accountEntityMapper;
    private final CustomerEntityMapper customerEntityMapper;


    public Account createAccount(CreateAccountRequest req) {
        Customer c = customerRepository.findById(req.getCustomerId())
                .map(customerEntityMapper::toDomain)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + req.getCustomerId()));
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

    public synchronized Account deposit(UUID accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        Account a = getAccountForUpdate(accountId);
        synchronized (a) {
            BigDecimal newBal = a.getBalance().add(amount);
            a.setBalance(newBal);
            Transaction tx = new Transaction(UUID.randomUUID(), Instant.now(), Transaction.Type.DEPOSIT, amount, newBal);
            a.addTransaction(tx);
            accountRepository.save(accountEntityMapper.toEntity(a));
            return a;
        }
    }

    public synchronized Account withdraw(UUID accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        Account a = getAccountForUpdate(accountId);
        synchronized (a) {
            if (a.getBalance().compareTo(amount) < 0) {
                throw new BadRequestException("Insufficient funds");
            }
            BigDecimal newBal = a.getBalance().subtract(amount);
            a.setBalance(newBal);
            Transaction tx = new Transaction(UUID.randomUUID(), Instant.now(), Transaction.Type.WITHDRAW, amount, newBal);
            a.addTransaction(tx);
            accountRepository.save(accountEntityMapper.toEntity(a));
            return a;
        }
    }

    public BigDecimal getBalance(UUID accountId) {
        return getAccount(accountId).getBalance();
    }

    public List<Transaction> getLastTransactions(UUID accountId, int limit) {
        if (limit <= 0) limit = 10;
        return getAccount(accountId).getLatestTransactions(limit);
    }
}
