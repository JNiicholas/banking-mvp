package com.example.banking.auth;

import com.example.banking.entity.CustomerEntity;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OwnershipChecker {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    /**
     * Returns true if the account belongs to the caller identified by Keycloak (externalAuthId + realm).
     */
    public boolean isOwner(UUID accountId, UUID callerExternalAuthId, String realmName) {
        // Resolve the caller's internal customer id via Keycloak linkage
        Optional<UUID> callerCustomerId = customerRepository
                .findByExternalAuthIdAndExternalAuthRealm(callerExternalAuthId, realmName)
                .map(CustomerEntity::getId);

        // Check if the account is owned by that customer id
        return callerCustomerId.filter(uuid -> accountRepository.existsByIdAndCustomerId(accountId, uuid)).isPresent();


    }
}