package com.example.banking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "external_auth_id")
    private UUID externalAuthId;

    @Column(name = "external_auth_realm", length = 64)
    private String externalAuthRealm;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<AccountEntity> accounts = new ArrayList<>();
}