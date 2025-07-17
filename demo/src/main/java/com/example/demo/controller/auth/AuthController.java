// Refactored version of AuthController.java with logic preserved and reduced redundancy
package com.example.demo.controller.auth;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.Role.ERole;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.security.services.UserDetailsImpl;
import com.example.demo.security.services.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserRepository userRepository;
    @Autowired private UserDetailsServiceImpl userDetailsService;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder encoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        if (loginRequest == null) return badRequest("Yêu cầu đăng nhập không hợp lệ!");
        String username = Optional.ofNullable(loginRequest.getUsername()).orElse("");

        try {
            User user = userRepository.findByUsernameWithRoles(username).orElse(null);
            if (user == null || user.getEnabled() != null && !user.getEnabled()) {
                return unauthorized("Tên đăng nhập hoặc mật khẩu không đúng hoặc tài khoản bị vô hiệu hóa.");
            }

            logUserInfo(user);
            if (!encoder.matches(Optional.ofNullable(loginRequest.getPassword()).orElse(""), Optional.ofNullable(user.getPassword()).orElse(""))) {
                return unauthorized("Tên đăng nhập hoặc mật khẩu không đúng");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = loadUserDetails(username);
            String jwt = jwtUtils.generateJwtToken(authentication);
            List<String> roles = getRoles(userDetails);
            String refreshToken = generateRefreshToken(authentication);

            return ResponseEntity.ok(new JwtResponse(jwt, refreshToken, user.getId(), user.getUsername(),
                    Optional.ofNullable(user.getEmail()).orElse(""), roles));

        } catch (Exception e) {
            logError("AUTHENTICATION ERROR", e);
            return badRequest("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = Optional.ofNullable(request.getRefreshToken()).orElse("").trim();
        if (refreshToken.isEmpty()) return unauthorized("Thiếu refresh token");

        try {
            refreshToken = stripBearerPrefix(refreshToken);
            if (!jwtUtils.validateJwtToken(refreshToken)) return unauthorized("Refresh token không hợp lệ hoặc đã hết hạn");

            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            UserDetailsImpl userDetails = (UserDetailsImpl) loadUserDetails(username);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            return ResponseEntity.ok(new JwtResponse(
                    jwtUtils.generateJwtToken(auth),
                    generateRefreshToken(auth),
                    userDetails.getId(),
                    userDetails.getUsername(),
                    Optional.ofNullable(userDetails.getEmail()).orElse(""),
                    getRoles(userDetails)));

        } catch (Exception e) {
            logError("REFRESH TOKEN ERROR", e);
            return unauthorized("Không thể làm mới token. Vui lòng đăng nhập lại.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(new MessageResponse("Đăng xuất thành công! Vui lòng xóa token ở phía client."));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return unauthorized("Thiếu hoặc sai định dạng Authorization header");

        String token = authHeader.substring(7);
        if (!jwtUtils.validateJwtToken(token)) return unauthorized("Token không hợp lệ hoặc đã hết hạn");

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) loadUserDetails(jwtUtils.getUserNameFromJwtToken(token));
            return ResponseEntity.ok(new JwtResponse(token, null, userDetails.getId(), userDetails.getUsername(),
                    userDetails.getEmail(), getRoles(userDetails)));
        } catch (Exception e) {
            return unauthorized("Không tìm thấy thông tin người dùng");
        }
    }

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            if (userRepository.existsByUsername(signUpRequest.getUsername()))
                return badRequest("Error: Username is already taken!");
            if (userRepository.existsByEmail(signUpRequest.getEmail()))
                return badRequest("Error: Email is already in use!");

            User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()));
            user.setFullName(signUpRequest.getFullName());
            user.setPhone(signUpRequest.getPhone());

            User savedUser = userRepository.save(user);
            Set<String> strRoles = Optional.ofNullable(signUpRequest.getRoles()).orElse(Collections.emptySet());

            if (strRoles.isEmpty()) {
                assignRole(savedUser, ERole.ROLE_USER);
            } else {
                for (String role : strRoles) {
                    try {
                        assignRole(savedUser, ERole.valueOf("ROLE_" + role.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        assignRole(savedUser, ERole.ROLE_USER);
                    }
                }
            }
            userRepository.save(savedUser);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            logger.error("Error during user registration for username: {}", signUpRequest.getUsername(), e);
            return ResponseEntity.status(500).body(new MessageResponse("Internal server error during registration"));
        }
    }

    // === Utility methods ===
    private void assignRole(User user, ERole roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.addRole(role);
    }

    private UserDetails loadUserDetails(String username) {
        return userDetailsService.loadUserByUsername(username);
    }

    private List<String> getRoles(UserDetails userDetails) {
        return Optional.ofNullable(userDetails.getAuthorities()).orElse(List.of()).stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .toList();
    }

    private String generateRefreshToken(Authentication auth) {
        try {
            return jwtUtils.generateRefreshToken(auth);
        } catch (Exception e) {
            logger.error("Error generating refresh token: {}", e.getMessage());
            return "";
        }
    }

    private String stripBearerPrefix(String token) {
        String prefix = jwtUtils.getJwtTokenPrefix();
        return token.startsWith(prefix) ? token.substring(prefix.length()).trim() : token;
    }

    private void logError(String label, Exception e) {
        logger.error("===== {} =====", label);
        logger.error("Error type: {}", e.getClass().getName());
        logger.error("Error message: {}", e.getMessage());
        if (e.getCause() != null) logger.error("Root cause: {}", e.getCause().getMessage());
    }

    private void logUserInfo(User user) {
        logger.info("User found - Username: {}, ID: {}", user.getUsername(), user.getId());
        logger.info("User enabled status: {}", user.getEnabled());
        logger.info("User roles: {}", user.getRoles().stream()
                .map(role -> role.getName().name()).toList());
    }

    private ResponseEntity<MessageResponse> unauthorized(String msg) {
        return ResponseEntity.status(401).body(new MessageResponse(msg));
    }

    private ResponseEntity<MessageResponse> badRequest(String msg) {
        return ResponseEntity.badRequest().body(new MessageResponse(msg));
    }
}