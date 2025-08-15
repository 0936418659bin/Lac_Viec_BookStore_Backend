package com.example.demo.repository;

import com.example.demo.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    // Tìm tất cả ảnh của một sản phẩm
    List<ProductImage> findByProductId(Long productId);
    
    // Tìm ảnh đại diện của sản phẩm
    Optional<ProductImage> findByProductIdAndThumbnailTrue(Long productId);
    
    // Xóa tất cả ảnh của một sản phẩm
    void deleteAllByProductId(Long productId);
}
