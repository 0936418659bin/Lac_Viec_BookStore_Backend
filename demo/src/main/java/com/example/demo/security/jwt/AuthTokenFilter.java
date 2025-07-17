package com.example.demo.security.jwt;

import java.io.IOException;
import java.util.Enumeration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.security.services.UserDetailsServiceImpl;

/**
 * Bộ lọc xác thực JWT, kiểm tra và xác thực token JWT trong mỗi request
 */
public class AuthTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Log thông tin request để debug
            if (logger.isDebugEnabled()) {
                logRequestInfo(request);
            }
            
            // Lấy JWT từ request
            String jwt = parseJwt(request);
            
            if (jwt != null) {
                logger.debug("JWT token found in request");
                
                try {
                    // Xác thực token
                    if (jwtUtils.validateJwtToken(jwt)) {
                        String username = jwtUtils.getUserNameFromJwtToken(jwt);
                        
                        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            logger.debug("Loading user details for username: {}", username);
                            
                            // Tải thông tin người dùng từ database
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            
                            // Tạo đối tượng xác thực
                            UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());
                            
                            // Thêm thông tin chi tiết về request
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            
                            // Lưu thông tin xác thực vào SecurityContext
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.debug("Authenticated user: {}, roles: {}", 
                                username, 
                                userDetails.getAuthorities());
                        }
                    } else {
                        logger.warn("Invalid JWT token");
                    }
                } catch (UsernameNotFoundException e) {
                    logger.error("User not found: {}", e.getMessage());
                } catch (Exception e) {
                    logger.error("Error processing JWT token: {}", e.getMessage());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Stack trace:", e);
                    }
                }
            } else {
                logger.debug("No JWT token found in request");
            }
            
        } catch (Exception e) {
            logger.error("Error in JWT authentication filter: {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Stack trace:", e);
            }
        }
        
        // Tiếp tục chuỗi filter
        filterChain.doFilter(request, response);
    }
    
    /**
     * Trích xuất JWT từ header Authorization
     */
    private String parseJwt(HttpServletRequest request) {
        try {
            String headerAuth = request.getHeader("Authorization");
            
            if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
                String jwt = headerAuth.substring(7).trim();
                
                // Log một phần của token để debug (không log toàn bộ token vì lý do bảo mật)
                if (logger.isDebugEnabled() && jwt.length() > 10) {
                    logger.debug("JWT token found (first 10 chars): {}...", jwt.substring(0, 10));
                }
                
                return jwt;
            }
        } catch (Exception e) {
            logger.error("Error parsing JWT token: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Ghi log thông tin request để debug
     */
    private void logRequestInfo(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Không log giá trị của Authorization header vì lý do bảo mật
            if ("authorization".equalsIgnoreCase(headerName)) {
                logger.debug("  {}: [PROTECTED]", headerName);
            } else {
                logger.debug("  {}: {}", headerName, request.getHeader(headerName));
            }
        }
        logger.debug("===================");
    }
}
