package com.example.demo.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.demo.config.JwtProperties;
import com.example.demo.security.services.UserDetailsImpl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final JwtProperties jwtProperties;

    @Autowired
    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        // Lấy danh sách roles từ UserDetails
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        // Tạo claims và thêm roles vào
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        
        return buildToken(claims, userPrincipal.getUsername(), jwtProperties.getExpirationMs());
    }
    
    public String generateRefreshToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, userPrincipal.getUsername(), jwtProperties.getRefreshExpirationMs());
    }
    
    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(jwtProperties.getIssuer())
                .setAudience(jwtProperties.getAudience())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        String secret = jwtProperties.getSecret();
        logger.info("[JWT] Using secret (raw): " + secret + " (length: " + (secret != null ? secret.length() : 0) + ")");
            
        // Sử dụng secret trực tiếp dưới dạng UTF-8 bytes
        // Điều này đảm bảo tính tương thích với jwt.io
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(authToken);
            
            // Validate issuer
            if (!jwtProperties.getIssuer().equals(claims.getBody().getIssuer())) {
                logger.error("Invalid JWT issuer: {}", claims.getBody().getIssuer());
                return false;
            }
            
            // Validate audience
            if (!jwtProperties.getAudience().equals(claims.getBody().getAudience())) {
                logger.error("Invalid JWT audience: {}", claims.getBody().getAudience());
                return false;
            }
            
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
    
    public String getJwtTokenPrefix() {
        return jwtProperties.getTokenPrefix();
    }
    
    public String getJwtHeader() {
        return jwtProperties.getHeader();
    }
}