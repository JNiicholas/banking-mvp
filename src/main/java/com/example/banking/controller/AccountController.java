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

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Account API", description = "Operations related to bank accounts")
public class AccountController {
    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit money", description = "Deposits a given amount into the account and returns updated balance")
    public AccountResponse deposit(@PathVariable("id") UUID id, @RequestBody @Valid AmountRequest req, @AuthenticationPrincipal Jwt jwt) {
        Account deposit = accountService.deposit(
                id, req.amount(), callerExternalId(jwt), callerRealm(jwt)
        );
        return accountMapper.toResponse(deposit);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraws a given amount from the account if sufficient funds exist")
    public AccountResponse withdraw(@PathVariable("id") UUID id, @RequestBody @Valid AmountRequest req, @AuthenticationPrincipal Jwt jwt) {
        Account withdraw = accountService.withdraw(
                id, req.amount(), callerExternalId(jwt), callerRealm(jwt)
        );
        return accountMapper.toResponse(withdraw);
    }

    @PreAuthorize("@authz.canReadAccount(authentication, #id)")
    @GetMapping("/{id}")
    @Operation(summary = "Get account by id", description = "Fetches an account by its identifier")
    public AccountResponse getById(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt) {
        Account account = accountService.getAccount(
                id, callerExternalId(jwt), callerRealm(jwt)
        );
        return accountMapper.toResponse(account);
    }

    @PreAuthorize("@authz.canReadAccount(authentication, #id)")
    @GetMapping("/{id}/balance")
    @Operation(summary = "Get account balance", description = "Fetches the current balance of the account")
    public BigDecimal balance(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt) {
        return accountService.getBalance(
                id, callerExternalId(jwt), callerRealm(jwt)
        );
    }

    @PreAuthorize("@authz.canReadAccount(authentication, #id)")
    @GetMapping("/{id}/transactions")
    @Operation(summary = "List recent transactions", description = "Returns the last N transactions of the account (default 10)")
    public List<TransactionResponse> lastTransactions(@PathVariable("id") UUID id,
                                                      @RequestParam(name="limit", defaultValue="10") int limit,
                                                      @AuthenticationPrincipal Jwt jwt) {
        List<Transaction> txs = accountService.getLastTransactions(
                id, limit, callerExternalId(jwt), callerRealm(jwt)
        );
        return txs.stream().map(transactionMapper::toResponse).collect(Collectors.toList());
    }

    private UUID callerExternalId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private String callerRealm(Jwt jwt) {
        String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
        if (iss == null) return null;
        int i = iss.lastIndexOf("/realms/");
        return (i >= 0) ? iss.substring(i + 8) : iss;
    }
}
