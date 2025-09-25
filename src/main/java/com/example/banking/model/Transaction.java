package com.example.banking.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Transaction {
    public enum Type { DEPOSIT, WITHDRAW }

    private UUID id;
    private UUID accountId;
    private Instant timestamp;
    private Type type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;


    public Transaction(UUID id, UUID accountId, Instant timestamp, Type type, BigDecimal amount, BigDecimal balanceAfter) {
        this.id = id;
        this.accountId = accountId;
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    public UUID getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public Type getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
}
