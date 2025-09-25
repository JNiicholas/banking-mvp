package com.example.banking.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateAccountRequest {
    @NotNull
    private UUID customerId;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
}
