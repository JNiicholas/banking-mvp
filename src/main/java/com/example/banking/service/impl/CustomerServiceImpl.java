package com.example.banking.service.impl;

import com.example.banking.keycloak.dto.CreateUserRequest;
import com.example.banking.keycloak.service.KeycloakProvisioningService;
import org.springframework.beans.factory.annotation.Value;

import com.example.banking.dto.CreateCustomerRequest;
import com.example.banking.entity.CustomerEntity;
import com.example.banking.exception.NotFoundException;
import com.example.banking.mapper.CustomerEntityMapper;
import com.example.banking.model.Customer;
import com.example.banking.repository.CustomerRepository;
import com.example.banking.service.api.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerEntityMapper customerEntityMapper;
    private final KeycloakProvisioningService keycloakProvisioningService;

    @Value("${keycloak.realm:}")
    private String keycloakRealm;

    @Override
    public Customer createCustomer(CreateCustomerRequest req) {
        // Build Keycloak user request from customer input
        var kcReq = CreateUserRequest.builder()
                .username(req.email())
                .email(req.email())
                .firstName(req.firstName())
                .lastName(req.lastName())
                .enabled(true)
                .build();

        //Create the user in keycloak
        String keycloakUserId = keycloakProvisioningService.createUser(kcReq);
        // Showcase: set a simple initial password for the user (not for production)
        keycloakProvisioningService.setUserPassword(keycloakUserId, "1234", false);
        UUID externalAuthId = UUID.fromString(keycloakUserId);

        // 2) Persist customer linked to Keycloak identity
        var entity = CustomerEntity.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .externalAuthId(externalAuthId)
                .externalAuthRealm(keycloakRealm)
                .build();

        return customerEntityMapper.toDomain(customerRepository.save(entity));
    }

    @Transactional(readOnly = true)
    @Override
    public Customer getCustomer(UUID id) {
        return customerRepository.findOneById(id)
                .map(customerEntityMapper::toDomain)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + id));
    }


    @Transactional(readOnly = true)
    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllWithAccounts()
                .stream()
                .map(customerEntityMapper::toDomain)
                .toList();
    }
}