package com.example.banking.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public record TransactionResponse(
        UUID id,
        Instant timestamp,
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter
) {}
