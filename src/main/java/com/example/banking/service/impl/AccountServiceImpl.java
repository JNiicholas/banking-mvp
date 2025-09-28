package com.example.banking.service.impl;

import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.entity.AccountEntity;
import com.example.banking.exception.BadRequestException;
import com.example.banking.exception.NotFoundException;
import com.example.banking.iban.IbanGenerator;
import com.example.banking.mapper.AccountEntityMapper;
import com.example.banking.mapper.CustomerEntityMapper;
import com.example.banking.mapper.TransactionEntityMapper;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.model.Transaction;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.service.api.AccountService;
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
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final AccountEntityMapper accountEntityMapper;
    private final CustomerEntityMapper customerEntityMapper;
    private final TransactionEntityMapper transactionEntityMapper;
    private final IbanGenerator ibanGenerator;


    @Override
    @Transactional
    public Account createAccount(CreateAccountRequest req) {
        // Validate the customer exists without triggering lazy collections
        if (!customerRepository.existsById(req.customerId())) {
            throw new NotFoundException("Customer not found: " + req.customerId());
        }

        Account a = Account.builder()
                .customerId(req.customerId())
                .balance(BigDecimal.ZERO)
                .build();

        // Map to entity
        AccountEntity toSave = accountEntityMapper.toNewEntity(a);

        // Generate IBAN independent of UUID and set BEFORE persisting
        var iban = ibanGenerator.generateNew();
        toSave.setIbanCountry(iban.country());
        toSave.setIbanNormalized(iban.normalized());
        toSave.setIbanDisplay(iban.display());

        // Single insert
        AccountEntity saved = accountRepository.save(toSave);
        return accountEntityMapper.toDomain(saved);
    }

    @Override
    public Account getAccount(UUID id, UUID callerExternalId, String callerRealm) {
        // Read-only: authorization is handled by @PreAuthorize at the controller level
        AccountEntity entity = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found: " + id));
        return accountEntityMapper.toDomain(entity);
    }


    @Override
    @Transactional
    public Account deposit(UUID accountId, BigDecimal amount, UUID callerExternalId, String callerRealm) {
        validateAmount(amount);
        UUID callerCustomerId = resolveCallerCustomerId(callerExternalId, callerRealm);

        AccountEntity entity = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));
        if (!entity.getCustomer().getId().equals(callerCustomerId)) {
            throw new NotFoundException("Account not found: " + accountId);
        }

        BigDecimal newBal = entity.getBalance().add(amount);
        entity.setBalance(newBal);
        accountRepository.save(entity);

        Transaction tx = new Transaction(null, accountId, Instant.now(), Transaction.Type.DEPOSIT, amount, newBal);
        transactionRepository.save(transactionEntityMapper.toEntity(tx));

        return accountEntityMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public Account withdraw(UUID accountId, BigDecimal amount, UUID callerExternalId, String callerRealm) {
        validateAmount(amount);
        UUID callerCustomerId = resolveCallerCustomerId(callerExternalId, callerRealm);

        AccountEntity entity = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));
        if (!entity.getCustomer().getId().equals(callerCustomerId)) {
            throw new NotFoundException("Account not found: " + accountId);
        }

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

    @Override
    public BigDecimal getBalance(UUID accountId, UUID callerExternalId, String callerRealm) {
        // Read-only: authorization is handled by @PreAuthorize at the controller level
        return accountRepository.findById(accountId)
                .map(AccountEntity::getBalance)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));
    }

    @Override
    public List<Transaction> getLastTransactions(UUID accountId, int limit, UUID callerExternalId, String callerRealm) {
        // Authorization is enforced at the controller via @PreAuthorize("@authz.canReadAccount(...)")
        // Keep NotFound semantics without triggering lazy loads
        if (!accountRepository.existsById(accountId)) {
            throw new NotFoundException("Account not found: " + accountId);
        }

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

    private UUID resolveCallerCustomerId(UUID externalAuthId, String realm) {
        if (externalAuthId == null || realm == null) {
            throw new BadRequestException("Missing caller identity");
        }
        return customerRepository.findByExternalAuthIdAndExternalAuthRealm(externalAuthId, realm)
                .map(com.example.banking.entity.CustomerEntity::getId)
                .orElseThrow(() -> new NotFoundException("Customer for caller not found"));
    }
}
