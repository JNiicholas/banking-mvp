package com.example.banking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountSummary(
        UUID id,
        BigDecimal balance
) {}