package com.example.banking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
public class AccountEntity {
    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
    @Column(nullable=false, precision=19, scale=4)
    private BigDecimal balance;

    @Column(name = "iban_country", nullable = false, length = 2)
    private String ibanCountry;

    @Column(name = "iban_normalized", nullable = false, length = 34, unique = true)
    private String ibanNormalized;

    @Column(name = "iban_display", length = 42) // optional pretty format
    private String ibanDisplay;

    @Version
    private Long version;
}