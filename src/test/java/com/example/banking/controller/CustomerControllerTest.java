package com.example.banking.controller;

import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.dto.CustomerResponse;
import com.example.banking.exception.NotFoundException;
import com.example.banking.mapper.CustomerMapper;
import com.example.banking.model.Customer;
import com.example.banking.service.api.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean
    CustomerService customerService;   // use interface, not impl
    @MockBean CustomerMapper customerMapper;     // controller depends on mapper for DTO mapping

    @Test
    @DisplayName("POST /customers -> 201 with body & (optional) Location header")
    void createCustomer_ok() throws Exception {
        // arrange
        var id = UUID.randomUUID();
        var req = new CreateCustomerRequest("Jonas Iqbal", "jonas@iqbal.dk");
        var domain = new Customer(id, "Jonas Iqbal", "jonas@iqbal.dk");
        var dto = new CustomerResponse(id, "Jonas Iqbal", "jonas@iqbal.dk");

        given(customerService.createCustomer(eq(req))).willReturn(domain);
        given(customerMapper.toResponse(eq(domain))).willReturn(dto);

        // act + assert
        mvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                // Adjust status depending on your controller:
                // If your controller returns 201 Created, keep isCreated();
                // If it returns 200 OK, change to status().isOk()
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Jonas Iqbal"))
                .andExpect(jsonPath("$.email").value("jonas@iqbal.dk"))
                // If your controller sets Location: /customers/{id}, this will pass. If not, remove it.
                .andExpect(header().string("Location", "/customers/" + id));
    }

    @Test
    @DisplayName("POST /customers -> 400 when validation fails")
    void createCustomer_validationError() throws Exception {
        // name is blank, email is invalid
        var badReq = new CreateCustomerRequest(" ", "not-an-email");

        mvc.perform(post("/customers")
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
        var domain = new Customer(id, "Alice", "alice@example.com");
        var dto = new CustomerResponse(id, "Alice", "alice@example.com");

        given(customerService.getCustomer(eq(id))).willReturn(domain);
        given(customerMapper.toResponse(eq(domain))).willReturn(dto);

        mvc.perform(get("/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Alice"))
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
        var c1 = new Customer(id1, "Alice", "alice@example.com");
        var c2 = new Customer(id2, "Bob", "bob@example.com");
        var d1 = new CustomerResponse(id1, "Alice", "alice@example.com");
        var d2 = new CustomerResponse(id2, "Bob", "bob@example.com");

        given(customerService.getAllCustomers()).willReturn(List.of(c1, c2));
        given(customerMapper.toResponse(eq(c1))).willReturn(d1);
        given(customerMapper.toResponse(eq(c2))).willReturn(d2);

        mvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[1].id").value(id2.toString()))
                .andExpect(jsonPath("$[1].name").value("Bob"))
                .andExpect(jsonPath("$[1].email").value("bob@example.com"));
    }
}