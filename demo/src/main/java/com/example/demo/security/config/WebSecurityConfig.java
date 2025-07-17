package com.example.demo.security.config;

import com.example.demo.security.jwt.AuthEntryPointJwt;
import com.example.demo.security.jwt.AuthTokenFilter;
import com.example.demo.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WebSecurityConfig.class);
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        logger.info("===== CONFIGURING DAO AUTHENTICATION PROVIDER =====");
        logger.info("1. Creating DaoAuthenticationProvider");
        
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider() {
            @Override
            protected void additionalAuthenticationChecks(UserDetails userDetails,
                    UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
                
                try {
                    super.additionalAuthenticationChecks(userDetails, authentication);
                    if (logger.isDebugEnabled()) {
                        logger.debug("9. Additional authentication checks passed");
                    }
                } catch (AuthenticationException e) {
                    logger.error("Xác thực thất bại: " + e.getMessage());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Authentication failure details:", e);
                    }
                    throw e;
                }
            }
        };

        logger.info("2. Setting user details service");
        authProvider.setUserDetailsService(userDetailsService);
        
        logger.info("3. Setting password encoder");
        PasswordEncoder encoder = passwordEncoder();
        logger.info("3.1. Password encoder class: {}", encoder.getClass().getName());
        authProvider.setPasswordEncoder(encoder);
        
        // Bắt lỗi để hiển thị thông báo rõ ràng hơn
        authProvider.setHideUserNotFoundExceptions(false);
        
        logger.info("4. DaoAuthenticationProvider configured successfully");
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("===== CONFIGURING SPRING SECURITY =====");
        
        // Tắt CSRF vì chúng ta sử dụng JWT
        http.csrf(csrf -> csrf.disable())
            // Xử lý lỗi xác thực
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(unauthorizedHandler)
            )
            // Cấu hình session không lưu trữ trạng thái (stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Cấu hình phân quyền truy cập
            .authorizeHttpRequests(auth -> 
                auth
                    // Cho phép truy cập công khai các endpoint đăng nhập, đăng ký, refresh token
                    .requestMatchers(
                        "/api/auth/signin",
                        "/api/auth/signup",
                        "/api/auth/refreshtoken",
                        "/api/auth/verify",
                            "/api/auth/logout",
                        "/api/test/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api/actuator/health" // Cho phép public health endpoint
                    ).permitAll()
                    // Yêu cầu xác thực cho tất cả các request khác
                    .anyRequest().authenticated()
            )
            // Tắt cache cho các response nhạy cảm
            .headers(headers -> 
                headers
                    .cacheControl(cache -> cache.disable())
                    .frameOptions(frame -> frame.sameOrigin())
                    .httpStrictTransportSecurity(hsts -> 
                        hsts.includeSubDomains(true).preload(true)
                    )
            )
            // Thêm bộ lọc JWT trước bộ lọc xác thực mặc định
            .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        // Cấu hình CORS nếu cần
        http.cors(cors -> {})
            // Thêm authentication provider
            .authenticationProvider(authenticationProvider());
            
        logger.info("Spring Security configuration completed");
        return http.build();
    }
}
