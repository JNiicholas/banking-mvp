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
        // Derive first/last from the provided name (best-effort)
        String firstName = req.name();
        String lastName = "";
        if (firstName != null) {
            String[] parts = firstName.trim().split("\\s+", 2);
            firstName = parts[0];
            if (parts.length > 1) {
                lastName = parts[1];
            }
        }

        // 1) Create user in Keycloak (admin flow) and obtain the KC userId (JWT `sub`)
        var kcReq = new CreateUserRequest();
        kcReq.setUsername(req.email());
        kcReq.setEmail(req.email());
        kcReq.setFirstName(firstName);
        kcReq.setLastName(lastName);
        kcReq.setEnabled(true);

        String keycloakUserId = keycloakProvisioningService.createUser(kcReq);
        // Showcase: set a simple initial password (not for production)
        keycloakProvisioningService.setUserPassword(keycloakUserId, "1234", false);
        UUID externalAuthId = UUID.fromString(keycloakUserId);

        // 2) Persist customer linked to Keycloak identity
        var entity = CustomerEntity.builder()
                .name(req.name())
                .email(req.email())
                .externalAuthId(externalAuthId)
                .externalAuthRealm(keycloakRealm)
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