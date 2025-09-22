package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import org.hibernate.annotations.BatchSize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", schema = "bookstore",
    uniqueConstraints = { 
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email") 
    })
@NamedEntityGraph(
    name = "User.userRoles",
    attributeNodes = @NamedAttributeNode(value = "userRoles", subgraph = "userRoles.role"),
    subgraphs = @NamedSubgraph(
        name = "userRoles.role",
        attributeNodes = @NamedAttributeNode("role")
    )
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    @JsonIgnore
    private String password;

    @Size(max = 100)
    @Column(name = "full_name")
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Column(name = "enabled")
    private Boolean enabled = Boolean.TRUE;

    @Column(name = "last_login")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    @JsonIgnore
    private Set<UserRole> userRoles = new HashSet<>();
    
    @Transient
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    
    public Set<Role> getRoles() {
        
        if (userRoles == null || userRoles.isEmpty()) {
            logger.info("No roles found for user {}", username);
            return new HashSet<>();
        }
        // Tạo một bản sao để tránh ConcurrentModificationException
        return new HashSet<>(userRoles).stream()
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void addRole(Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(this);
        userRole.setRole(role);
        // Đảm bảo set UserRoleId đầy đủ
        if (this.getId() != null && role.getId() != null) {
            userRole.setId(new UserRoleId(this.getId(), role.getId()));
        }
        userRoles.add(userRole);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
        this.updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
