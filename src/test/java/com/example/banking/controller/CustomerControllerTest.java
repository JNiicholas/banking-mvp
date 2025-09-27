package com.example.banking.controller;

import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.dto.CustomerResponse;
import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.exception.NotFoundException;
import com.example.banking.mapper.CustomerMapper;
import com.example.banking.mapper.AccountMapper;
import com.example.banking.model.Customer;
import com.example.banking.model.Account;
import com.example.banking.service.api.CustomerService;
import com.example.banking.service.api.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(CustomerController.class)
@WithMockUser(username = "testuser", roles = "ADMIN")
class CustomerControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    CustomerService customerService;   // use interface, not impl

    @MockitoBean
    CustomerMapper customerMapper;     // controller depends on mapper for DTO mapping

    @MockitoBean
    AccountService accountService;

    @MockitoBean
    AccountMapper accountMapper;

    @Test
    @DisplayName("POST /customers -> 201 with body & (optional) Location header")
    void createCustomer_ok() throws Exception {
        // arrange
        var id = UUID.randomUUID();
        var req = CreateCustomerRequest.builder()
                .firstName("Jonas")
                .lastName("Iqbal")
                .email("jonas@iqbal.dk")
                .build();
        var domain = new Customer();
        domain.setId(id);
        domain.setFirstName("Jonas");
        domain.setLastName("Iqbal");
        domain.setEmail("jonas@iqbal.dk");
        domain.setExternalAuthId(null);
        domain.setExternalAuthRealm(null);
        var dto = new CustomerResponse(id, "Jonas", "Iqbal", "jonas@iqbal.dk", null, null);

        given(customerService.createCustomer(eq(req))).willReturn(domain);
        given(customerMapper.toResponse(eq(domain))).willReturn(dto);

        // act + assert
        mvc.perform(post("/customers").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                // Adjust status depending on your controller:
                // If your controller returns 201 Created, keep isCreated();
                // If it returns 200 OK, change to status().isOk()
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("Jonas"))
                .andExpect(jsonPath("$.lastName").value("Iqbal"))
                .andExpect(jsonPath("$.email").value("jonas@iqbal.dk"))
                // If your controller sets Location: /customers/{id}, this will pass. If not, remove it.
                .andExpect(header().string("Location", "/customers/" + id));
    }

    @Test
    @DisplayName("POST /customers -> 400 when validation fails")
    void createCustomer_validationError() throws Exception {
        // firstName is blank, email is invalid
        var badReq = CreateCustomerRequest.builder()
                .firstName(" ")
                .lastName("Iqbal")
                .email("not-an-email")
                .build();

        mvc.perform(post("/customers").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badReq)))
                .andExpect(status().isBadRequest());
        // Optionally assert error structure produced by your GlobalExceptionHandler
        // .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("GET /customers/{id} -> 200")
    void getCustomer_found() throws Exception {
        var id = UUID.randomUUID();
        var domain = new Customer();
        domain.setId(id);
        domain.setFirstName("Alice");
        domain.setLastName("Smith");
        domain.setEmail("alice@example.com");
        domain.setExternalAuthId(null);
        domain.setExternalAuthRealm(null);
        var dto = new CustomerResponse(id, "Alice", "Smith", "alice@example.com", null, null);

        given(customerService.getCustomer(eq(id))).willReturn(domain);
        given(customerMapper.toResponse(eq(domain))).willReturn(dto);

        mvc.perform(get("/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("GET /customers/{id} -> 404 when not found")
    void getCustomer_notFound() throws Exception {
        var id = UUID.randomUUID();
        given(customerService.getCustomer(eq(id))).willThrow(new NotFoundException("Customer not found"));

        mvc.perform(get("/customers/{id}", id))
                .andExpect(status().isNotFound());
        // If your GlobalExceptionHandler returns an ApiError JSON:
        // .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /customers -> 200 list")
    void listCustomers_ok() throws Exception {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var c1 = new Customer();
        c1.setId(id1);
        c1.setFirstName("Alice");
        c1.setLastName("Smith");
        c1.setEmail("alice@example.com");
        c1.setExternalAuthId(null);
        c1.setExternalAuthRealm(null);

        var c2 = new Customer();
        c2.setId(id2);
        c2.setFirstName("Bob");
        c2.setLastName("Brown");
        c2.setEmail("bob@example.com");
        c2.setExternalAuthId(null);
        c2.setExternalAuthRealm(null);

        var d1 = new CustomerResponse(id1, "Alice", "Smith", "alice@example.com", null, null);
        var d2 = new CustomerResponse(id2, "Bob", "Brown", "bob@example.com", null, null);

        given(customerService.getAllCustomers()).willReturn(List.of(c1, c2));
        given(customerMapper.toResponse(eq(c1))).willReturn(d1);
        given(customerMapper.toResponse(eq(c2))).willReturn(d2);

        mvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[0].firstName").value("Alice"))
                .andExpect(jsonPath("$[0].lastName").value("Smith"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[1].id").value(id2.toString()))
                .andExpect(jsonPath("$[1].firstName").value("Bob"))
                .andExpect(jsonPath("$[1].lastName").value("Brown"))
                .andExpect(jsonPath("$[1].email").value("bob@example.com"));
    }
    @Test
    @DisplayName("POST /customers/accounts -> 201 Created with body (+optional Location)")
    void createAccount_ok() throws Exception {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        var req = new CreateAccountRequest(customerId);
        var domain = Account.builder().id(id).customerId(customerId).balance(new BigDecimal("0.0000")).build();
        var dto = new AccountResponse(id, customerId, new BigDecimal("0.0000"));

        given(accountService.createAccount(eq(req))).willReturn(domain);
        given(accountMapper.toResponse(eq(domain))).willReturn(dto);

        mvc.perform(post("/customers/accounts").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.balance").value(0.0));
        // If your controller sets Location, you can add:
        // .andExpect(header().string("Location", "/accounts/" + id));
    }
}