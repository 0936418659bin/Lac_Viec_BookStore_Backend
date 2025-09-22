// Refactored version of AuthController.java with logic preserved and reduced redundancy
package com.example.demo.controller.api.v1.auth;

import com.example.demo.dto.auth.request.LoginRequest;
import com.example.demo.dto.auth.request.RefreshTokenRequest;
import com.example.demo.dto.auth.request.SignupRequest;
import com.example.demo.dto.auth.response.JwtResponse;
import com.example.demo.dto.common.MessageResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.Role.ERole;
import com.example.demo.repository.auth.RoleRepository;
import com.example.demo.repository.auth.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.service.common.FileStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private FileStorageService fileStorageService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getUsername());
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
                    Optional.ofNullable(user.getEmail()).orElse(""),
                    Optional.ofNullable(user.getAvatar()).orElse(""),
                    roles));

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
            if (!jwtUtils.validateJwtToken(refreshToken))
                return unauthorized("Refresh token không hợp lệ hoặc đã hết hạn");

            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            UserDetailsImpl userDetails = (UserDetailsImpl) loadUserDetails(username);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            return ResponseEntity.ok(new JwtResponse(
                    jwtUtils.generateJwtToken(auth),
                    generateRefreshToken(auth),
                    userDetails.getId(),
                    userDetails.getUsername(),
                    Optional.ofNullable(userDetails.getEmail()).orElse(""),
                    Optional.ofNullable(userDetails.getAvatar()).orElse(""),
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
                    userDetails.getEmail(), Optional.ofNullable(userDetails.getAvatar()).orElse(""), getRoles(userDetails)));
        } catch (Exception e) {
            return unauthorized("Không tìm thấy thông tin người dùng");
        }
    }

    @PostMapping(value = "/signup", consumes = {"application/json"})
    @Transactional
    public ResponseEntity<?> registerUserJson(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("Received JSON signup request for user: {}", signUpRequest.getUsername());

        try {
            // Validate input
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return badRequest("Error: Username is already taken!");
            }
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return badRequest("Error: Email is already in use!");
            }

            // Create and save user
            User user = createUserFromRequest(signUpRequest);

            // Assign default role if not provided
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                assignDefaultRole(user);
            }

            // Save user to database
            User savedUser = userRepository.save(user);
            logger.info("User registered successfully: {}", savedUser.getUsername());

            // Create UserDetails for token generation
            UserDetails userDetails = UserDetailsImpl.build(savedUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // Generate tokens
            String jwt = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(authentication);

            // Prepare response
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    refreshToken,
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getAvatar(),
                    roles
            ));

        } catch (Exception e) {
            logError("REGISTRATION ERROR", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/signup", consumes = {"multipart/form-data"})
    @Transactional
    public ResponseEntity<?> registerUser(
            @RequestPart(value = "userData", required = false) String userDataJson,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {

        logger.info("Received signup request");

        try {
            // 1. Parse request data
            SignupRequest signUpRequest = parseSignupRequest(userDataJson, username, email, password, fullName, phone);
            if (signUpRequest == null) {
                return badRequest("Invalid user data. Please provide either userData JSON or individual fields");
            }

            // 2. Validate input
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return badRequest("Error: Username is already taken!");
            }
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return badRequest("Error: Email is already in use!");
            }

            // 3. Handle avatar upload
            String avatarPath = handleAvatarUpload(avatarFile);
            if (avatarPath != null) {
                signUpRequest.setAvatar(avatarPath);
            }

            // 4. Create and save user
            User user = createUserFromRequest(signUpRequest);

            // 5. Assign default role
            assignDefaultRole(user);

            // 6. Save user to database
            User savedUser = userRepository.save(user);
            logger.info("User registered successfully: {}", savedUser.getUsername());

            // 7. Create UserDetails for token generation
            UserDetails userDetails = UserDetailsImpl.build(savedUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // 8. Generate tokens
            String jwt = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(authentication);

            // 8. Tạo URL đầy đủ cho ảnh đại diện
            String avatarUrl = savedUser.getAvatar() != null ?
                    "/api/avatar/" + savedUser.getAvatar().substring(savedUser.getAvatar().lastIndexOf('/') + 1) :
                    null;

            // 9. Trả về response với URL ảnh đại diện đầy đủ
            return buildSuccessResponse(jwt, refreshToken, savedUser, avatarUrl);

        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    new MessageResponse("Error: " + e.getMessage())
            );
        }
    }

    private SignupRequest parseSignupRequest(String userDataJson, String username, String email,
                                             String password, String fullName, String phone) {
        SignupRequest signUpRequest = new SignupRequest();

        // Try to parse from JSON first
        if (userDataJson != null && !userDataJson.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                signUpRequest = objectMapper.readValue(userDataJson, SignupRequest.class);
                logger.info("Parsed signup request from JSON");
                return signUpRequest;
            } catch (JsonProcessingException e) {
                logger.error("Error parsing userData JSON: {}", e.getMessage());
                return null;
            }
        }
        // Fall back to individual fields
        else if (username != null && email != null && password != null) {
            signUpRequest.setUsername(username);
            signUpRequest.setEmail(email);
            signUpRequest.setPassword(password);
            signUpRequest.setFullName(fullName);
            signUpRequest.setPhone(phone);
            logger.info("Created signup request from individual fields");
            return signUpRequest;
        }

        return null;
    }

    private String handleAvatarUpload(MultipartFile avatarFile) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            return null;
        }

        try {
            return fileStorageService.storeFile(avatarFile);
        } catch (Exception e) {
            logger.error("Could not store avatar: {}", e.getMessage());
            return null; // Continue without avatar if upload fails
        }
    }

    private User createUserFromRequest(SignupRequest signUpRequest) {
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setFullName(signUpRequest.getFullName());
        user.setPhone(signUpRequest.getPhone());
        user.setAvatar(signUpRequest.getAvatar());
        assignDefaultRole(user); // Gán role mặc định
        return user;
    }

    private void assignDefaultRole(User user) {
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Default role ROLE_USER not found"));
        user.getRoles().add(userRole);
    }

    private ResponseEntity<?> buildSuccessResponse(String jwt, String refreshToken, User user) {
        return buildSuccessResponse(jwt, refreshToken, user, null);
    }

    private ResponseEntity<?> buildSuccessResponse(String jwt, String refreshToken, User user, String avatarUrl) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        // Sử dụng avatarUrl nếu được cung cấp, nếu không dùng avatar từ user
        String finalAvatarUrl = (avatarUrl != null) ? avatarUrl : user.getAvatar();

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                finalAvatarUrl,  // Sử dụng URL ảnh đã được xử lý
                roles
        ));
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