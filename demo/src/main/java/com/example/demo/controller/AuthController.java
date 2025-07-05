package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.TokenRefreshException;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.AuthService;
import com.example.demo.service.RefreshTokenService;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        // Xác thực thông tin đăng nhập
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Tạo token và refresh token
        String accessToken = tokenProvider.generateJwtToken(authentication);
        String refreshToken = refreshTokenService.createRefreshToken(
            ((UserPrincipal) authentication.getPrincipal()).getId()
        ).getToken();
        
        User user = userService.getUserById(((UserPrincipal) authentication.getPrincipal()).getId());
        
        return ResponseEntity.ok(AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(authService.registerUser(registerRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken);
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);
        
        // Tạo mới access token
        String token = tokenProvider.generateTokenFromUsername(
            refreshToken.getUser().getUsername(), 
            tokenProvider.getJwtExpirationMs()
        );
        
        return ResponseEntity.ok(TokenRefreshResponse.builder()
            .accessToken(token)
            .refreshToken(refreshToken.getToken())
            .build());
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@Valid @RequestBody TokenRefreshRequest request) {
        refreshTokenService.revokeToken(request.getRefreshToken());
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        
        User user = userService.getUserById(userPrincipal.getId());
        
        return ResponseEntity.ok(AuthResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .build());
    }
    
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<?> handleTokenRefreshException(TokenRefreshException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(HttpStatus.FORBIDDEN, e.getMessage()));
    }
}