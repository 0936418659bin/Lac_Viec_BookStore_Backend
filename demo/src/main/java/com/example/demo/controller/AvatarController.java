package com.example.demo.controller;

import com.example.demo.security.services.UserDetailsImpl;
import com.example.demo.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/avatar")
public class AvatarController {
    private static final Logger logger = LoggerFactory.getLogger(AvatarController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            // Lấy thông tin người dùng hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
                logger.warn("Unauthorized access attempt to avatar: {}", filename);
                return ResponseEntity.status(401).build();
            }

            // Lấy thông tin người dùng từ authentication
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Kiểm tra xem người dùng có quyền xem ảnh này không
            // Ở đây chúng ta có thể kiểm tra thêm nếu cần
            // Ví dụ: Chỉ cho phép xem ảnh của chính mình hoặc admin
            if (!userDetails.getAvatar().contains(filename) && 
                !userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                logger.warn("User {} is not authorized to access avatar: {}", 
                          userDetails.getUsername(), filename);
                return ResponseEntity.status(403).build();
            }

            // Tải file ảnh
            Path filePath = Paths.get(System.getProperty("user.dir"), "uploads/avatars")
                               .resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Hoặc xác định đúng loại MIME
                    .body(resource);
            } else {
                logger.error("Could not read file: {}", filename);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving avatar: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
