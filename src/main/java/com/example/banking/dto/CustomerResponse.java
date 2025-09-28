package com.example.banking.dto;

import java.util.List;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        UUID externalAuthId,
        String externalAuthRealm,
        List<AccountSummary> accounts
) {}