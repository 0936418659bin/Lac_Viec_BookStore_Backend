package com.example.demo.service.user;

import com.example.demo.dto.user.response.UserDTO;
import com.example.demo.dto.user.request.UpdateUserRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.exception.common.ResourceNotFoundException;
import com.example.demo.repository.auth.RoleRepository;
import com.example.demo.repository.auth.UserRepository;
import com.example.demo.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request, UserDetailsImpl currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        boolean isSelf = currentUser.getId().equals(id);

        // Basic info that any user can update about themselves
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        // Username update with uniqueness check
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username is already taken!");
            }
            user.setUsername(request.getUsername());
        }
        
        // Email update with uniqueness check
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email is already in use!");
            }
            user.setEmail(request.getEmail());
        }

        // Password change
        if (request.getCurrentPassword() != null && request.getNewPassword() != null) {
            if (!isAdmin && !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        // Admin-only fields
        if (isAdmin) {
            if (request.getEnabled() != null) {
                user.setEnabled(request.getEnabled());
            }
            
            // Update roles if provided
            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                // Clear existing roles
                user.getUserRoles().clear();
                
                // Add new roles
                Set<UserRole> userRoles = request.getRoles().stream()
                        .map(roleName -> {
                            Role.ERole roleEnum = Role.ERole.valueOf(roleName);
                            Role role = roleRepository.findByName(roleEnum)
                                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                            UserRole userRole = new UserRole();
                            userRole.setUser(user);
                            userRole.setRole(role);
                            return userRole;
                        })
                        .collect(Collectors.toSet());
                
                user.setUserRoles(userRoles);
            }
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }



    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setRoles(user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName().name())
                .collect(Collectors.toSet()));
        return dto;
    }
}