package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * Entity for storing refresh tokens
 */
@Entity
@Table(name = "refresh_tokens")
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
    
    @Column(nullable = false, unique = true, length = 500)
    private String token;
    
    @Column(nullable = false)
    private Instant expiryDate;
    
    @Column(nullable = false)
    private boolean revoked = false;
    
    @Column(nullable = false)
    private boolean expired = false;
    
    public boolean isActive() {
        return !revoked && !expired && expiryDate.isAfter(Instant.now());
    }
}
