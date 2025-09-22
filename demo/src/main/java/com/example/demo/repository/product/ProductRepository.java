package com.example.demo.repository.product;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Tìm các sản phẩm đang trong chương trình flash sale
    List<Product> findByIsFlashSaleTrue();
    
    // Giảm số lượng tồn kho
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :productId AND p.stockQuantity >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);
    
    // Cập nhật trạng thái flash sale
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.isFlashSale = :isFlashSale WHERE p.id = :productId")
    void updateFlashSaleStatus(@Param("productId") Long productId, @Param("isFlashSale") boolean isFlashSale);
    
    // Các phương thức tùy chỉnh có thể được thêm vào đây nếu cần
}
