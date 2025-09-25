package com.example.banking.service;

import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.model.Customer;
import com.example.banking.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(CreateCustomerRequest req) {
        Customer c = new Customer(UUID.randomUUID(), req.getName(), req.getEmail());
        return customerRepository.save(c);
    }

    public Customer get(UUID id) {
        return customerRepository.findById(id).orElse(null);
    }
}
