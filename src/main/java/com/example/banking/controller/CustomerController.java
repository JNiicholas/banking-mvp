package com.example.banking.controller;

import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.dto.CustomerResponse;
import com.example.banking.mapper.CustomerMapper;
import com.example.banking.model.Account;
import com.example.banking.model.Customer;
import com.example.banking.service.api.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.mapper.AccountMapper;
import com.example.banking.service.api.AccountService;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customer API", description = "Operations related to customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new customer", description = "Creates a new customer and returns the customer details")
    // TODO: Use the injected 'jwt' for logging/tracing (e.g., log sub/email/issuer)
    public ResponseEntity<CustomerResponse> create(@RequestBody @Valid CreateCustomerRequest req, @AuthenticationPrincipal Jwt jwt) {
        Customer customer = customerService.createCustomer(req);
        CustomerResponse body = customerMapper.toResponse(customer);
        return ResponseEntity.created(URI.create("/customers/" + body.id())).body(body);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Get customer by id", description = "Returns a single customer by its id")
    // TODO: Use the injected 'jwt' for logging/tracing (e.g., log sub/email/issuer)
    public CustomerResponse getById(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt) {
        Customer customer = customerService.getCustomer(id);
        return customerMapper.toResponse(customer);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "List customers", description = "Returns all customers")
    // TODO: Use the injected 'jwt' for logging/tracing (e.g., log sub/email/issuer)
    public List<CustomerResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return customerService.getAllCustomers().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new account", description = "Creates a new account for an existing customer")
    // TODO: Use the injected 'jwt' for logging/tracing (e.g., log sub/email/issuer)
    public AccountResponse createAccount(@RequestBody @Valid CreateAccountRequest req, @AuthenticationPrincipal Jwt jwt) {
        Account account = accountService.createAccount(req);
        return accountMapper.toResponse(account);
    }
}
