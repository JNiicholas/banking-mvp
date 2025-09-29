package com.example.banking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountSummaryResponse(
        UUID id,
        BigDecimal balance,
        String ibanCountry,
        String ibanNormalized,
        String ibanDisplay
) {}
