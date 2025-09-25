package com.example.banking.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    private UUID id;
    private UUID customerId;

    @Setter
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    // Optional convenience ctor (keeps your previous behavior)
    public Account(UUID id, UUID customerId) {
        this.id = id;
        this.customerId = customerId;
        this.balance = BigDecimal.ZERO;
    }
}