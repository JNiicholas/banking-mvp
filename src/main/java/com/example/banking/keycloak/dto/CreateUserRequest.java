package com.example.banking.keycloak.dto;

import lombok.Builder;

/** Payload to create a Keycloak user via Admin REST API */
@Builder
public record CreateUserRequest(
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled
) {}