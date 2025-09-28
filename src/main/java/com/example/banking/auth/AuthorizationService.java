package com.example.banking.auth;

import com.example.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("authz")
@RequiredArgsConstructor
public class AuthorizationService {

    private final OwnershipChecker ownershipChecker;

    @Value("${keycloak.realm}")
    private String realmName;

    public boolean canReadAccount(Authentication authentication, UUID accountId) {
        // 1. Check if user has ADMIN role
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        String callerSub = ((JwtAuthenticationToken) authentication).getToken().getSubject();
        UUID callerExternalId = UUID.fromString(callerSub);
        return ownershipChecker.isOwner(accountId, callerExternalId, realmName);
    }
}
