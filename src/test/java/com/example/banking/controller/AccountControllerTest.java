package com.example.banking.controller;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.AmountRequest;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.exception.NotFoundException;
import com.example.banking.mapper.AccountMapper;
import com.example.banking.mapper.TransactionMapper;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.service.api.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(AccountController.class)
@WithMockUser(username = "testuser", roles = "USER")
class AccountControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AccountService accountService;     // use interface
    @MockitoBean AccountMapper accountMapper;       // controller uses this to map domain -> DTO
    @MockitoBean TransactionMapper transactionMapper;

    @Test
    @DisplayName("GET /accounts/{id} -> 200 with AccountResponse")
    void getById_ok() throws Exception {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        var domain = Account.builder().id(id).customerId(customerId).balance(new BigDecimal("100.0000")).build();
        var dto = new AccountResponse(id, customerId, new BigDecimal("100.0000"));

        UUID callerExternalId = UUID.randomUUID();
        String callerRealm = "BankingApp";

        BDDMockito.given(accountService.getAccount(eq(id), eq(callerExternalId), eq(callerRealm))).willReturn(domain);
        BDDMockito.given(accountMapper.toResponse(eq(domain))).willReturn(dto);

        mvc.perform(get("/accounts/{id}", id)
                        .with(jwt().jwt(j -> {
                            j.subject(callerExternalId.toString());
                            j.issuer("http://localhost:8081/realms/BankingApp");
                        }).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.balance").value(100.0));
    }

    @Test
    @DisplayName("GET /accounts/{id} -> 404 when not found")
    void getById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        UUID callerExternalId = UUID.randomUUID();
        String callerRealm = "BankingApp";

        BDDMockito.given(accountService.getAccount(eq(id), eq(callerExternalId), eq(callerRealm)))
                .willThrow(new NotFoundException("Account not found"));

        mvc.perform(get("/accounts/{id}", id)
                        .with(jwt().jwt(j -> {
                            j.subject(callerExternalId.toString());
                            j.issuer("http://localhost:8081/realms/BankingApp");
                        }).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
        // If you assert ApiError shape, also check $.code == NOT_FOUND, etc.
    }

    @Test
    @DisplayName("POST /accounts/{id}/deposit -> 200 with updated AccountResponse")
    void deposit_ok() throws Exception {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        UUID callerExternalId = UUID.randomUUID();
        String callerRealm = "BankingApp";

        var req = new AmountRequest(new BigDecimal("50.00"));
        var updated = Account.builder().id(id).customerId(customerId).balance(new BigDecimal("150.0000")).build();
        var dto = new AccountResponse(id, customerId, new BigDecimal("150.0000"));

        BDDMockito.given(accountService.deposit(eq(id), eq(req.amount()), eq(callerExternalId), eq(callerRealm)))
                .willReturn(updated);
        BDDMockito.given(accountMapper.toResponse(eq(updated))).willReturn(dto);

        mvc.perform(post("/accounts/{id}/deposit", id).with(csrf())
                        .with(jwt().jwt(j -> {
                            j.subject(callerExternalId.toString());
                            j.issuer("http://localhost:8081/realms/BankingApp");
                        }).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(150.0));
    }

    @Test
    @DisplayName("POST /accounts/{id}/withdraw -> 200 with updated AccountResponse")
    void withdraw_ok() throws Exception {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        UUID callerExternalId = UUID.randomUUID();
        String callerRealm = "BankingApp";

        var req = new AmountRequest(new BigDecimal("25.00"));
        var updated = Account.builder().id(id).customerId(customerId).balance(new BigDecimal("75.0000")).build();
        var dto = new AccountResponse(id, customerId, new BigDecimal("75.0000"));

        BDDMockito.given(accountService.withdraw(eq(id), eq(req.amount()), eq(callerExternalId), eq(callerRealm)))
                .willReturn(updated);
        BDDMockito.given(accountMapper.toResponse(eq(updated))).willReturn(dto);

        mvc.perform(post("/accounts/{id}/withdraw", id).with(csrf())
                        .with(jwt().jwt(j -> {
                            j.subject(callerExternalId.toString());
                            j.issuer("http://localhost:8081/realms/BankingApp");
                        }).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(75.0));
    }

    @Test
    @DisplayName("GET /accounts/{id}/transactions?limit=10 -> 200 with list")
    void listTransactions_ok() throws Exception {
        UUID id = UUID.randomUUID();
        UUID callerExternalId = UUID.randomUUID();
        String callerRealm = "BankingApp";

        var tx1 = new Transaction(UUID.randomUUID(), id, Instant.parse("2025-01-01T00:00:00Z"),
                Transaction.Type.DEPOSIT, new BigDecimal("100.00"), new BigDecimal("100.0000"));
        var tx2 = new Transaction(UUID.randomUUID(), id, Instant.parse("2025-01-02T00:00:00Z"),
                Transaction.Type.WITHDRAW, new BigDecimal("20.00"), new BigDecimal("80.0000"));

        var dto1 = new TransactionResponse(tx1.getId(), tx1.getTimestamp(), "DEPOSIT",
                tx1.getAmount(), tx1.getBalanceAfter());
        var dto2 = new TransactionResponse(tx2.getId(), tx2.getTimestamp(), "WITHDRAW",
                tx2.getAmount(), tx2.getBalanceAfter());

        BDDMockito.given(accountService.getLastTransactions(eq(id), eq(10), eq(callerExternalId), eq(callerRealm)))
                .willReturn(List.of(tx1, tx2));
        BDDMockito.given(transactionMapper.toResponse(eq(tx1))).willReturn(dto1);
        BDDMockito.given(transactionMapper.toResponse(eq(tx2))).willReturn(dto2);

        mvc.perform(get("/accounts/{id}/transactions", id)
                        .param("limit", "10")
                        .with(jwt().jwt(j -> {
                            j.subject(callerExternalId.toString());
                            j.issuer("http://localhost:8081/realms/BankingApp");
                        }).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(tx1.getId().toString()))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[1].type").value("WITHDRAW"));
    }
}