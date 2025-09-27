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
        // Build Keycloak user request from customer input
        NameParts np = splitName(req.name());
        var kcReq = CreateUserRequest.builder()
                .username(req.email())
                .email(req.email())
                .firstName(np.first())
                .lastName(np.last())
                .enabled(true)
                .build();

        //Create the user in keycloak
        String keycloakUserId = keycloakProvisioningService.createUser(kcReq);
        // Showcase: set a simple initial password for the user (not for production)
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
    private static NameParts splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new NameParts("", "");
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        String first = parts[0];
        String last = parts.length > 1 ? parts[1] : "";
        return new NameParts(first, last);
    }

    private record NameParts(String first, String last) {}
}