package com.example.banking.service.api;

import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.model.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    Customer createCustomer(CreateCustomerRequest req);
    Customer getCustomer(UUID id);
    List<Customer> getAllCustomers();
}