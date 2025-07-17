package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    private Set<String> roles;
    
    private String fullName;
    
    private String phone;

    public SignupRequest() {
        this.roles = new HashSet<>();
    }

    public void setRoles(Set<String> roles) {
        if (roles == null) {
            this.roles = new HashSet<>();
        } else {
            this.roles = roles;
        }
    }
}
