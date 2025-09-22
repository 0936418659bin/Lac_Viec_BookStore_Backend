package com.example.demo.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @Size(max = 50, message = "Full name must not exceed 50 characters")
    private String fullName;

    @Email(message = "Email should be valid")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;

    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phone;

    private String avatar;
    
    private Boolean enabled;
    
    private Set<String> roles;
    
    // Only used when changing password
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String currentPassword;
    
    @Size(min = 6, max = 40, message = "New password must be between 6 and 40 characters")
    private String newPassword;
}