package com.example.banking.controller;

import com.example.banking.dto.*;
import com.example.banking.mapper.TransactionMapper;
import com.example.banking.model.Account;
import com.example.banking.mapper.AccountMapper;
import com.example.banking.model.Transaction;
import com.example.banking.service.api.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Account API", description = "Operations related to bank accounts")
public class AccountController {
    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new account", description = "Creates a new account for an existing customer")
    public AccountResponse create(@RequestBody @Valid CreateAccountRequest req) {
        Account account = accountService.createAccount(req);
        return accountMapper.toResponse(account);
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit money", description = "Deposits a given amount into the account and returns updated balance")
    public AccountResponse deposit(@PathVariable("id") UUID id, @RequestBody @Valid AmountRequest req) {
        Account deposit = accountService.deposit(id, req.amount());
        return accountMapper.toResponse(deposit);
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraws a given amount from the account if sufficient funds exist")
    public AccountResponse withdraw(@PathVariable("id") UUID id, @RequestBody @Valid AmountRequest req) {
        Account withdraw = accountService.withdraw(id, req.amount());
        return accountMapper.toResponse(withdraw);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by id", description = "Fetches an account by its identifier")
    public AccountResponse getById(@PathVariable("id") UUID id) {
        Account account = accountService.getAccount(id);
        return accountMapper.toResponse(account);
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get account balance", description = "Fetches the current balance of the account")
    public BigDecimal balance(@PathVariable("id") UUID id) {
        return accountService.getBalance(id);
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "List recent transactions", description = "Returns the last N transactions of the account (default 10)")
    public List<TransactionResponse> lastTransactions(@PathVariable("id") UUID id,
                                                      @RequestParam(name="limit", defaultValue="10") int limit) {
        List<Transaction> txs = accountService.getLastTransactions(id, limit);
        return txs.stream().map(transactionMapper::toResponse).collect(Collectors.toList());
    }
}
