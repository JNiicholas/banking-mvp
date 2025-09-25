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
    @Column(nullable=false)
    private UUID customerId;
    @Column(nullable=false, precision=19, scale=4)
    private BigDecimal balance;

    @Version
    private long version;
}