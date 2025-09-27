package com.example.banking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = @UniqueConstraint(name = "uk_customers_email", columnNames = "email")
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 255)
    private String email;

    // Keycloak linkage (Option A): store the KC userId (JWT `sub`) and optional realm
    // Note: Uniqueness & partial index are enforced via Flyway migration (DB-level)
    @Column(name = "external_auth_id")
    private UUID externalAuthId; // nullable by design

    @Column(name = "external_auth_realm", length = 64)
    private String externalAuthRealm; // nullable; useful if multiple realms are used
}