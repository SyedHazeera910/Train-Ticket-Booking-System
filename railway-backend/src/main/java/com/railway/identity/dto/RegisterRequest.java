package com.railway.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.Pattern;

@Data
public class RegisterRequest {
    @NotBlank @Size(min = 2, max = 50) private String name;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 6) private String password;
    @NotBlank @Pattern(regexp = "^\\d{10}$", message = "Phone must be a 10-digit number") private String phone;
}
