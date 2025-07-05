package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.User;
import org.springframework.security.core.Authentication;

public interface AuthService {
    /**
     * Xác thực người dùng
     */
    Authentication authenticateUser(AuthRequest loginRequest);
    
    /**
     * Đăng ký người dùng mới
     */
    AuthResponse registerUser(RegisterRequest registerRequest);
    
    /**
     * Làm mới access token
     */
    TokenRefreshResponse refreshToken(String refreshToken);
    
    /**
     * Đăng xuất
     */
    void logout(String refreshToken);
    
    /**
     * Lấy thông tin người dùng hiện tại
     */
    User getCurrentUser();
}
