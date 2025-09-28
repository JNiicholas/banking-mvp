
package com.example.banking.controller;

import com.example.banking.auth.AuthorizationService;

import com.example.banking.dto.AccountResponse;
import com.example.banking.mapper.AccountMapper;
import com.example.banking.mapper.TransactionMapper;
import com.example.banking.model.Account;
import com.example.banking.service.api.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc
@Import(AccountControllerSecurityTest.MethodSecurityTestConfig.class)
class AccountControllerSecurityTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean(name = "authz")
    AuthorizationService authz; // used by @PreAuthorize

    @MockitoBean
    AccountService accountService; // controller dependency

    // Mappers are controller dependencies; mock to avoid wiring MapStruct
    @MockitoBean
    TransactionMapper transactionMapper;

    @MockitoBean
    AccountMapper accountMapper;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithRole(String role) {
        String sub = UUID.randomUUID().toString();
        String issuer = "http://localhost:8081/realms/BankingApp"; // matches controller's expected realm source
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(j -> j.subject(sub).issuer(issuer))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    @DisplayName("USER owner: can read transactions -> 200")
    void userOwner_canReadTransactions_200() throws Exception {
        UUID accountId = UUID.randomUUID();
        // Authorization: owner -> allowed
        given(authz.canReadAccount(any(), eq(accountId))).willReturn(true);
        // Service: return empty list to avoid mapping
        given(accountService.getLastTransactions(eq(accountId), anyInt(), any(), any())).willReturn(List.of());

        mvc.perform(get("/accounts/{id}/transactions", accountId)
                        .param("limit", "10")
                        .with(jwtWithRole("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("USER non-owner: forbidden -> 403")
    void userNotOwner_forbidden_403() throws Exception {
        UUID accountId = UUID.randomUUID();
        given(authz.canReadAccount(any(), eq(accountId))).willReturn(false);

        mvc.perform(get("/accounts/{id}/transactions", accountId)
                        .param("limit", "5")
                        .with(jwtWithRole("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN: can read balance of any account -> 200")
    void admin_canReadBalance_200() throws Exception {
        UUID accountId = UUID.randomUUID();
        given(authz.canReadAccount(any(), eq(accountId))).willReturn(true);
        given(accountService.getBalance(eq(accountId), any(), any())).willReturn(BigDecimal.TEN);

        mvc.perform(get("/accounts/{id}/balance", accountId)
                        .with(jwtWithRole("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unauthenticated: 401 on balance")
    void unauthenticated_balance_401() throws Exception {
        UUID accountId = UUID.randomUUID();
        mvc.perform(get("/accounts/{id}/balance", accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN: can read account by id -> 200")
    void admin_canReadAccountById_200() throws Exception {
        UUID accountId = UUID.randomUUID();
        given(authz.canReadAccount(any(), eq(accountId))).willReturn(true);
        // Return mocked domain and response to satisfy controller mapping
        Account domain = Mockito.mock(Account.class);
        AccountResponse response = Mockito.mock(AccountResponse.class);
        given(accountService.getAccount(eq(accountId), any(), any())).willReturn(domain);
        given(accountMapper.toResponse(eq(domain))).willReturn(response);

        mvc.perform(get("/accounts/{id}", accountId)
                        .with(jwtWithRole("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
        // No beans needed; enabling method security is enough for @PreAuthorize to run
    }
}
