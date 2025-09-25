package com.example.banking.controller;

import com.example.banking.dto.*;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@RequestBody @Valid CreateAccountRequest req) {
        Account a = accountService.createAccount(req);
        return new AccountResponse(a.getId(), a.getCustomerId(), a.getBalance());
    }

    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable("id") UUID id, @RequestBody @Valid AmountRequest req) {
        Account a = accountService.deposit(id, req.getAmount());
        return new AccountResponse(a.getId(), a.getCustomerId(), a.getBalance());
    }

    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable("id") UUID id, @RequestBody @Valid AmountRequest req) {
        Account a = accountService.withdraw(id, req.getAmount());
        return new AccountResponse(a.getId(), a.getCustomerId(), a.getBalance());
    }

    @GetMapping("/{id}/balance")
    public BigDecimal balance(@PathVariable("id") UUID id) {
        return accountService.getBalance(id);
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> lastTransactions(@PathVariable("id") UUID id,
                                                      @RequestParam(name="limit", defaultValue="10") int limit) {
        List<Transaction> txs = accountService.getLastTransactions(id, limit);
        return txs.stream().map(t -> new TransactionResponse(
                t.getId(), t.getTimestamp(), t.getType().name(), t.getAmount(), t.getBalanceAfter()
        )).collect(Collectors.toList());
    }
}
