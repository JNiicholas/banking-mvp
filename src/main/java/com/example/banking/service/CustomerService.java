package com.example.banking.service;

import com.example.banking.entity.CustomerEntity;
import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.model.Customer;
import com.example.banking.mapper.CustomerEntityMapper;
import com.example.banking.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerEntityMapper customerEntityMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerEntityMapper customerEntityMapper) {
        this.customerRepository = customerRepository;
        this.customerEntityMapper = customerEntityMapper;
    }

    public Customer createCustomer(CreateCustomerRequest req) {
        Customer c = new Customer(UUID.randomUUID(), req.getName(), req.getEmail());
        CustomerEntity saved = customerRepository.save(customerEntityMapper.toEntity(c));
        return customerEntityMapper.toDomain(saved);
    }

    public Customer get(UUID id) {
        return customerRepository.findById(id)
                .map(customerEntityMapper::toDomain)
                .orElse(null);
    }
}
