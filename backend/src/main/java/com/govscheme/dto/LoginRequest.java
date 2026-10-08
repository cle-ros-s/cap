package com.govscheme.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email or mobile number is required")
    private String username; // Can be email or mobile

    @NotBlank(message = "Password is required")
    private String password;
}
