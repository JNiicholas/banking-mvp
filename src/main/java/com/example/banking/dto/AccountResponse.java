package com.example.banking.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record AccountResponse(
        UUID id,
        UUID customerId,
        BigDecimal balance,
        String ibanCountry,
        String ibanNormalized,
        String ibanDisplay
) {}
