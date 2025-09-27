package com.example.banking.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload to create a Keycloak user via Admin REST API */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled = true;
}