package com.example.banking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    public enum Type { DEPOSIT, WITHDRAW }

    private UUID id;
    private UUID accountId;
    private Instant timestamp;
    private Type type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
}
