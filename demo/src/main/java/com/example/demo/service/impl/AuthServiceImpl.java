package com.example.demo.service.impl;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.RoleName;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Override
    public Authentication authenticateUser(AuthRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setPhone(registerRequest.getPhone());

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", RoleName.ROLE_USER));
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        User result = userRepository.save(user);

        // Generate tokens
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                registerRequest.getUsername(),
                registerRequest.getPassword()
        );
        
        String accessToken = tokenProvider.generateJwtToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(result.getId())
                .username(result.getUsername())
                .email(result.getEmail())
                .fullName(result.getFullName())
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateJwtToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String username = tokenProvider.getUserNameFromJwtToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Generate new tokens
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                UserPrincipal.create(user),
                null,
                user.getAuthorities()
        );
        
        String newAccessToken = tokenProvider.generateJwtToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}
