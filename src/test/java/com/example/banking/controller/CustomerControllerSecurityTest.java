package com.example.banking.controller;


import com.example.banking.dto.CustomerResponse;
import com.example.banking.mapper.CustomerMapper;
import com.example.banking.mapper.AccountMapper;

import com.example.banking.config.SecurityConfig;
import com.example.banking.model.Customer;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import com.example.banking.service.api.AccountService;
import com.example.banking.service.api.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
public class CustomerControllerSecurityTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CustomerService customerService;

    @MockitoBean
    AccountService accountService;

    @MockitoBean
    CustomerMapper customerMapper;

    @MockitoBean
    AccountMapper accountMapper;

    @MockitoBean
    JwtDecoder jwtDecoder;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithRole(String role) {
        String sub = UUID.randomUUID().toString();
        String issuer = "http://localhost:8081/realms/BankingApp";
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(j -> j.subject(sub).issuer(issuer))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    @DisplayName("ADMIN: can list customers -> 200")
    void admin_listCustomers_200() throws Exception {
        given(customerService.getAllCustomers()).willReturn(List.of());

        mvc.perform(get("/customers").with(jwtWithRole("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("USER: list customers forbidden -> 403")
    void user_listCustomers_403() throws Exception {
        mvc.perform(get("/customers").with(jwtWithRole("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated: list customers -> 401")
    void unauthenticated_listCustomers_401() throws Exception {
        mvc.perform(get("/customers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN: get customer by id -> 200")
    void admin_getCustomer_200() throws Exception {
        UUID id = UUID.randomUUID();
        Customer domain = Mockito.mock(Customer.class);
        CustomerResponse response = Mockito.mock(CustomerResponse.class);
        given(customerService.getCustomer(eq(id))).willReturn(domain);
        given(customerMapper.toResponse(eq(domain))).willReturn(response);

        mvc.perform(get("/customers/{id}", id).with(jwtWithRole("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("USER: get customer by id forbidden -> 403")
    void user_getCustomer_403() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(get("/customers/{id}", id).with(jwtWithRole("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated: get customer by id -> 401")
    void unauthenticated_getCustomer_401() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(get("/customers/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN: create customer -> 2xx")
    void admin_createCustomer_2xx() throws Exception {
        String body = "{\n" +
                "  \"firstName\": \"Alice\",\n" +
                "  \"lastName\": \"Smith\",\n" +
                "  \"email\": \"alice@example.com\"\n" +
                "}";

        Customer domain = Mockito.mock(Customer.class);
        CustomerResponse response = Mockito.mock(CustomerResponse.class);
        given(response.id()).willReturn(UUID.randomUUID());
        given(customerService.createCustomer(any())).willReturn(domain);
        given(customerMapper.toResponse(any())).willReturn(response);

        mvc.perform(post("/customers")
                        .with(jwtWithRole("ADMIN"))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("USER: create customer forbidden -> 403")
    void user_createCustomer_403() throws Exception {
        String body = "{\n" +
                "  \"firstName\": \"Alice\",\n" +
                "  \"lastName\": \"Smith\",\n" +
                "  \"email\": \"alice@example.com\"\n" +
                "}";
        mvc.perform(post("/customers")
                        .with(jwtWithRole("USER"))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated: create customer -> 401")
    void unauthenticated_createCustomer_401() throws Exception {
        String body = "{\n" +
                "  \"firstName\": \"Alice\",\n" +
                "  \"lastName\": \"Smith\",\n" +
                "  \"email\": \"alice@example.com\"\n" +
                "}";
        mvc.perform(post("/customers")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
