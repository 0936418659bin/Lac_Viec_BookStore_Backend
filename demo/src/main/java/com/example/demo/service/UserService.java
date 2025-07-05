package com.example.demo.service;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.RoleName;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for user management operations
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users with pagination
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Create a new user
     */
    @Transactional
    public User createUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFullName(userDto.getFullName());
        user.setPhone(userDto.getPhone());
        user.setEnabled(true);

        // Set default role as ROLE_USER
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    /**
     * Update an existing user
     */
    @Transactional
    public User updateUser(Long id, UserDto userDto, UserPrincipal currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check if the current user is the owner or an admin
        if (!currentUser.isAdmin() && !user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't have permission to update this user");
        }

        if (userRepository.existsByUsernameAndIdNot(userDto.getUsername(), id)) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), id)) {
            throw new BadRequestException("Email is already in use!");
        }

        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFullName(userDto.getFullName());
        user.setPhone(userDto.getPhone());
        
        // Only update password if it's provided
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * Delete a user
     */
    @Transactional
    public void deleteUser(Long id, UserPrincipal currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check if the current user is the owner or an admin
        if (!currentUser.isAdmin() && !user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("You don't have permission to delete this user");
        }

        userRepository.delete(user);
    }

    /**
     * Update user roles (admin only)
     */
    @Transactional
    public User updateUserRoles(Long userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(RoleName.valueOf(roleName))
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        return userRepository.save(user);
    }

    /**
     * Check if a user has a specific role
     */
    public boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
}