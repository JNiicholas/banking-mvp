package com.example.banking.service.api;

import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    Account createAccount(CreateAccountRequest req);
    Account getAccount(UUID id);
    Account deposit(UUID accountId, BigDecimal amount);
    Account withdraw(UUID accountId, BigDecimal amount);
    BigDecimal getBalance(UUID accountId);
    List<Transaction> getLastTransactions(UUID accountId, int limit);
}