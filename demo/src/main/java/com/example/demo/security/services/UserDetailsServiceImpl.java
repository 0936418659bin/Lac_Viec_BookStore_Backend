package com.example.demo.security.services;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.auth.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByUsernameWithRoles(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

            Set<Role> roles = new HashSet<>();

            try {
                List<Role> roleList = entityManager.createQuery(
                                "SELECT DISTINCT r FROM User u " +
                                        "JOIN u.userRoles ur " +
                                        "JOIN ur.role r " +
                                        "WHERE u.username = :username", Role.class)
                        .setParameter("username", username)
                        .getResultList();

                roles = new HashSet<>(roleList);
            } catch (Exception e) {
                try {
                    Set<UserRole> userRoles = user.getUserRoles();
                    if (userRoles != null) {
                        roles = userRoles.stream()
                                .map(UserRole::getRole)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());
                    }
                } catch (Exception ignored) {
                }
            }

            List<GrantedAuthority> authorities = roles.stream()
                    .filter(role -> role != null && role.getName() != null)
                    .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                    .collect(Collectors.toList());

            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            String password = user.getPassword() != null ? user.getPassword() : "";
            Boolean enabled = user.getEnabled();

            user.setEnabled(enabled);

            return UserDetailsImpl.build(user, authorities);
        } catch (Exception e) {
            throw e;
        }
    }
}
