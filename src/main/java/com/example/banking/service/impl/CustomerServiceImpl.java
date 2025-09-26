package com.example.banking.service.impl;

import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.entity.CustomerEntity;
import com.example.banking.exception.NotFoundException;
import com.example.banking.mapper.CustomerEntityMapper;
import com.example.banking.model.Customer;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.service.api.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerEntityMapper customerEntityMapper;

    @Override
    public Customer createCustomer(CreateCustomerRequest req) {
        var entity = CustomerEntity.builder()
                .name(req.name())
                .email(req.email())
                .build();
        return customerEntityMapper.toDomain(customerRepository.save(entity));
    }

    @Override
    public Customer getCustomer(UUID id) {
        return customerRepository.findById(id)
                .map(customerEntityMapper::toDomain)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + id));
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerEntityMapper::toDomain)
                .toList();
    }
}