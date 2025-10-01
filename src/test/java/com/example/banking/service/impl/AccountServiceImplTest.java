package com.example.banking.service.impl;

import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.dto.IbanResult;
import com.example.banking.entity.AccountEntity;
import com.example.banking.entity.CustomerEntity;
import com.example.banking.entity.TransactionEntity;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountEntityMapper accountEntityMapper;
    @Mock private TransactionEntityMapper transactionEntityMapper;
    @Mock private CustomerEntityMapper customerEntityMapper;
    @Mock private IbanGenerator ibanGenerator;

    @InjectMocks
    private AccountServiceImpl accountService; // class under test

    @Test
    void createAccount_ok() {
        UUID customerId = UUID.randomUUID();
        var req = new CreateAccountRequest(customerId);

        // customer exists
        given(customerRepository.existsById(eq(customerId))).willReturn(true);

        // IBAN generation for new accounts
        given(ibanGenerator.generateNew())
                .willReturn(new IbanResult("DE", "DE80270925559385021793", "DE80 2709 2555 9385 0217 93"));


        given(accountEntityMapper.toNewEntity(any(Account.class)))
                .willAnswer(inv -> {
                    Account a = inv.getArgument(0);
                    var e = new AccountEntity();
                    e.setId(UUID.randomUUID());
                    var ce = new CustomerEntity();
                    ce.setId(a.getCustomerId());
                    e.setCustomer(ce);
                    e.setBalance(a.getBalance());
                    e.setVersion(0L);
                    return e;
                });

        UUID accountId = UUID.randomUUID();
        var savedEntity = AccountEntityBuilder(accountId, customerId, bd("0.0000"));
        given(accountRepository.save(any(AccountEntity.class))).willReturn(savedEntity);

        var domain = Account.builder().id(accountId).customerId(customerId).balance(bd("0.0000")).build();
        given(accountEntityMapper.toDomain(savedEntity)).willReturn(domain);

        var result = accountService.createAccount(req);

        assertNotNull(result);
        assertEquals(accountId, result.getId());
        assertEquals(customerId, result.getCustomerId());
        assertEquals(0, result.getBalance().compareTo(bd("0.0000")));

        then(customerRepository).should().existsById(customerId);
        then(accountRepository).should().save(any(AccountEntity.class));
        then(accountEntityMapper).should().toDomain(savedEntity);
    }

    @Test
    void getAccount_ok() {
        UUID id = UUID.randomUUID(), customerId = UUID.randomUUID();
        var entity = AccountEntityBuilder(id, customerId, bd("100.0000"));
        var domain = Account.builder().id(id).customerId(customerId).balance(bd("100.0000")).build();

        given(accountRepository.findById(id)).willReturn(Optional.of(entity));
        given(accountEntityMapper.toDomain(entity)).willReturn(domain);

        var a = accountService.getAccount(id, null, null);

        assertEquals(id, a.getId());
        assertEquals(0, a.getBalance().compareTo(bd("100.0000")));
    }

    @Test
    void getAccount_notFound() {
        UUID id = UUID.randomUUID();
        given(accountRepository.findById(id)).willReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> accountService.getAccount(id, null, null));
    }

    @Test
    void deposit_ok() {
        UUID id = UUID.randomUUID(), customerId = UUID.randomUUID();
        UUID callerExternalId = UUID.randomUUID();
        String callerRealm = "BankingApp";
        var callerCustomerEntity = CustomerEntity.builder().id(customerId).firstName("C").lastName("O")
                .email("c@test.local").externalAuthId(callerExternalId).externalAuthRealm(callerRealm).build();
        given(customerRepository.findByExternalAuthIdAndExternalAuthRealm(eq(callerExternalId), eq(callerRealm)))
                .willReturn(Optional.of(callerCustomerEntity));

        var entityBefore = AccountEntityBuilder(id, customerId, bd("100.0000"));
        var entityAfter = AccountEntityBuilder(id, customerId, bd("150.0000"));
        var domainAfter = Account.builder().id(id).customerId(customerId).balance(bd("150.0000")).build();

        given(accountRepository.findByIdForUpdate(id)).willReturn(Optional.of(entityBefore));
        given(accountRepository.save(entityBefore)).willReturn(entityAfter);

        given(transactionEntityMapper.toEntity(any(Transaction.class)))
                .willAnswer(inv -> {
                    Transaction tx = inv.getArgument(0);
                    return TransactionEntity.builder()
                            .id(tx.getId() != null ? tx.getId() : UUID.randomUUID())
                            .accountId(tx.getAccountId())
                            .timestamp(tx.getTimestamp())
                            .type(tx.getType() == Transaction.Type.DEPOSIT ? TransactionEntity.Type.DEPOSIT : TransactionEntity.Type.WITHDRAW)
                            .amount(tx.getAmount())
                            .balanceAfter(tx.getBalanceAfter())
                            .build();
                });
        given(transactionRepository.save(any(TransactionEntity.class))).willAnswer(inv -> inv.getArgument(0));
        given(accountEntityMapper.toDomain(any(AccountEntity.class)))
                .willAnswer(inv -> {
                    AccountEntity e = inv.getArgument(0);
                    return Account.builder()
                            .id(e.getId())
                            .customerId(e.getCustomer().getId())
                            .balance(e.getBalance())
                            .build();
                });

        var out = accountService.deposit(id, bd("50.00"), callerExternalId, callerRealm);

        assertEquals(0, out.getBalance().compareTo(bd("150.0000")));
        then(accountRepository).should().findByIdForUpdate(id);
        then(accountRepository).should().save(entityBefore);
        then(transactionRepository).should().save(any(TransactionEntity.class));
    }

    @Test
    void withdraw_ok() {
        UUID id = UUID.randomUUID(), customerId = UUID.randomUUID();
        UUID callerExternalId = UUID.randomUUID();
        String callerRealm = "BankingApp";
        var callerCustomerEntity = CustomerEntity.builder().id(customerId).firstName("C").lastName("O")
                .email("c@test.local").externalAuthId(callerExternalId).externalAuthRealm(callerRealm).build();
        given(customerRepository.findByExternalAuthIdAndExternalAuthRealm(eq(callerExternalId), eq(callerRealm)))
                .willReturn(Optional.of(callerCustomerEntity));

        var entityBefore = AccountEntityBuilder(id, customerId, bd("100.0000"));
        var entityAfter = AccountEntityBuilder(id, customerId, bd("60.0000"));
        var domainAfter = Account.builder().id(id).customerId(customerId).balance(bd("60.0000")).build();

        given(accountRepository.findByIdForUpdate(id)).willReturn(Optional.of(entityBefore));
        given(accountRepository.save(entityBefore)).willReturn(entityAfter);
        // map domain tx -> entity so repository.save(...) does not get null
        given(transactionEntityMapper.toEntity(any(Transaction.class)))
                .willAnswer(inv -> {
                    Transaction tx = inv.getArgument(0);
                    return TransactionEntity.builder()
                            .id(tx.getId() != null ? tx.getId() : UUID.randomUUID())
                            .accountId(tx.getAccountId())
                            .timestamp(tx.getTimestamp())
                            .type(tx.getType() == Transaction.Type.DEPOSIT ? TransactionEntity.Type.DEPOSIT : TransactionEntity.Type.WITHDRAW)
                            .amount(tx.getAmount())
                            .balanceAfter(tx.getBalanceAfter())
                            .build();
                });
        given(transactionRepository.save(any(TransactionEntity.class))).willAnswer(inv -> inv.getArgument(0));
        given(accountEntityMapper.toDomain(any(AccountEntity.class)))
                .willAnswer(inv -> {
                    AccountEntity e = inv.getArgument(0);
                    return Account.builder()
                            .id(e.getId())
                            .customerId(e.getCustomer().getId())
                            .balance(e.getBalance())
                            .build();
                });

        var out = accountService.withdraw(id, bd("40.00"), callerExternalId, callerRealm);

        assertEquals(0, out.getBalance().compareTo(bd("60.0000")));
        then(accountRepository).should().findByIdForUpdate(id);
        then(transactionRepository).should().save(any(TransactionEntity.class));
    }

    @Test
    void withdraw_insufficientFunds() {
        UUID id = UUID.randomUUID(), customerId = UUID.randomUUID();
        UUID callerExternalId = UUID.randomUUID();
        String callerRealm = "BankingApp";
        var callerCustomerEntity = CustomerEntity.builder().id(customerId).firstName("C").lastName("O")
                .email("c@test.local").externalAuthId(callerExternalId).externalAuthRealm(callerRealm).build();
        given(customerRepository.findByExternalAuthIdAndExternalAuthRealm(eq(callerExternalId), eq(callerRealm)))
                .willReturn(Optional.of(callerCustomerEntity));

        var entity = AccountEntityBuilder(id, customerId, bd("10.00"));
        given(accountRepository.findByIdForUpdate(id)).willReturn(Optional.of(entity));

        assertThrows(BadRequestException.class, () -> accountService.withdraw(id, bd("20.00"), callerExternalId, callerRealm));

        then(accountRepository).should(never()).save(any(AccountEntity.class));
        then(transactionRepository).shouldHaveNoInteractions();
    }

    @Test
    void getBalance_ok() {
        UUID id = UUID.randomUUID(), customerId = UUID.randomUUID();
        var entity = AccountEntityBuilder(id, customerId, bd("123.4500"));
        given(accountRepository.findById(any(UUID.class))).willReturn(Optional.of(entity));

        var bal = accountService.getBalance(id, null, null);
        assertEquals(0, bal.compareTo(bd("123.4500")));
    }

    @Test
    void getLastTransactions_ok() {
        UUID accountId = UUID.randomUUID();
        UUID callerExternalId = UUID.randomUUID();
        String callerRealm = "BankingApp";
        UUID customerId = UUID.randomUUID();

        given(accountRepository.existsById(eq(accountId))).willReturn(true);

        var e1 = TransactionEntity.builder()
                .id(UUID.randomUUID()).accountId(accountId).timestamp(Instant.parse("2025-01-01T00:00:00Z"))
                .type(TransactionEntity.Type.DEPOSIT).amount(bd("10.00")).balanceAfter(bd("10.0000")).build();
        var e2 = TransactionEntity.builder()
                .id(UUID.randomUUID()).accountId(accountId).timestamp(Instant.parse("2025-01-02T00:00:00Z"))
                .type(TransactionEntity.Type.WITHDRAW).amount(bd("5.00")).balanceAfter(bd("5.0000")).build();

        var d1 = new Transaction(e1.getId(), e1.getAccountId(), e1.getTimestamp(),
                Transaction.Type.DEPOSIT, e1.getAmount(), e1.getBalanceAfter());
        var d2 = new Transaction(e2.getId(), e2.getAccountId(), e2.getTimestamp(),
                Transaction.Type.WITHDRAW, e2.getAmount(), e2.getBalanceAfter());

        given(transactionRepository.findRecentByAccountId(eq(accountId), any()))
                .willReturn(List.of(e1, e2));
        given(transactionEntityMapper.toDomain(e1)).willReturn(d1);
        given(transactionEntityMapper.toDomain(e2)).willReturn(d2);

        var out = accountService.getLastTransactions(accountId, 10, callerExternalId, callerRealm);
        assertEquals(2, out.size());
        assertEquals(d1.getId(), out.get(0).getId());
        assertEquals(d2.getId(), out.get(1).getId());
    }

    // helpers
    private static AccountEntity AccountEntityBuilder(UUID id, UUID customerId, BigDecimal balance) {
        var e = new AccountEntity();
        e.setId(id);
        var ce = new CustomerEntity();
        ce.setId(customerId);
        e.setCustomer(ce);
        e.setBalance(balance);
        e.setVersion(0L);
        return e;
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }
}