package com.railway.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDetailsResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
}
