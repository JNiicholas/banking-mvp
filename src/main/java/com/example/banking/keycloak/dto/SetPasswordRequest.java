package com.example.banking.keycloak.dto;

import lombok.Data;

/** Payload to set/reset a user's password */
@Data
public class SetPasswordRequest {
    private String type = "password";
    private String value;
    private boolean temporary = false;
}