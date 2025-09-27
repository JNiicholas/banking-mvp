package com.example.banking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    private UUID id;
    private String name;
    private String email;

    // Link to Keycloak user (JWT `sub`) and optional realm
    private UUID externalAuthId;      // nullable by design
    private String externalAuthRealm; // nullable; useful for multi-realm setups
}