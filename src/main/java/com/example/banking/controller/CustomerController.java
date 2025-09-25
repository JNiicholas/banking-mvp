package com.example.banking.controller;

import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.dto.CustomerResponse;
import com.example.banking.model.Customer;
import com.example.banking.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@RequestBody @Valid CreateCustomerRequest req) {
        Customer c = customerService.createCustomer(req);
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail());
    }
}
