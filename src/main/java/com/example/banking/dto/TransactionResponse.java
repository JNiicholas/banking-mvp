package com.example.banking.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TransactionResponse {
    private UUID id;
    private Instant timestamp;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;

    public TransactionResponse(UUID id, Instant timestamp, String type, BigDecimal amount, BigDecimal balanceAfter) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    public UUID getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
}
