package com.example.banking.controller;

import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.dto.CustomerResponse;
import com.example.banking.mapper.CustomerMapper;
import com.example.banking.model.Customer;
import com.example.banking.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customer API", description = "Operations related to customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new customer", description = "Creates a new customer and returns the customer details")
    public CustomerResponse create(@RequestBody @Valid CreateCustomerRequest req) {
        Customer customer = customerService.createCustomer(req);
        return customerMapper.toResponse(customer);
    }
}
