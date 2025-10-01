package com.example.demo.repository.product;

import com.example.demo.model.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    List<ProductImage> findByProductId(Long productId);

    long countByProductId(Long productId);  // Add this line

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductImage pi WHERE pi.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductImage pi WHERE pi.product.id = :productId AND pi.imageUrl = :imageUrl")
    void deleteByProductIdAndImageUrl(
            @Param("productId") Long productId,
            @Param("imageUrl") String imageUrl
    );
}
