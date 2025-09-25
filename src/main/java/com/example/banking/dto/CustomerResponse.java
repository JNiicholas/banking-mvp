package com.example.banking.dto;

import java.util.UUID;

public class CustomerResponse {
    private UUID id;
    private String name;
    private String email;

    public CustomerResponse(UUID id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
