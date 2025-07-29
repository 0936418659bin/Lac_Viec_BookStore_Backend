// Cleaned version of UserDetailsImpl.java with logging removed
package com.example.demo.security.services;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String email;
    private final String avatar;
    private final boolean enabled;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String avatar, String password,
                           Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public static UserDetailsImpl build(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        Set<Role> roles = user.getRoles() != null ? new HashSet<>(user.getRoles()) : new HashSet<>();

        for (Role role : roles) {
            if (role != null && role.getName() != null) {
                String roleName = role.getName().name();
                if (!roleName.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(roleName));
                }
            }
        }

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return build(user, authorities);
    }

    public static UserDetailsImpl build(User user, Collection<? extends GrantedAuthority> authorities) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        boolean isEnabled = user.getEnabled() != null && user.getEnabled();

        if (authorities == null || authorities.isEmpty()) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new UserDetailsImpl(
                user.getId(),
                Optional.ofNullable(user.getUsername()).orElse(""),
                Optional.ofNullable(user.getEmail()).orElse(""),
                Optional.ofNullable(user.getAvatar()).orElse(""),
                Optional.ofNullable(user.getPassword()).orElse(""),
                authorities,
                isEnabled
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
