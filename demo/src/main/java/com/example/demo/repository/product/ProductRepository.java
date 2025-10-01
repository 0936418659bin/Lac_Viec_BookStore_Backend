package com.example.demo.repository.product;

import com.example.demo.model.product.Product;
import com.example.demo.model.product.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    // Tìm sản phẩm theo danh mục với phân trang
    Page<Product> findByCategories_Id(Long categoryId, Pageable pageable);
    
    // Tìm sản phẩm theo loại sản phẩm với phân trang
    Page<Product> findByType(ProductType type, Pageable pageable);
    
    // Tìm kiếm sản phẩm theo từ khóa trong tên hoặc mô tả (không phân biệt hoa thường)
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name, String description, Pageable pageable);
    
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
    
    // Cập nhật số lượng tồn kho
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stockQuantity = :quantity WHERE p.id = :productId")
    int updateStock(@Param("productId") Long productId, @Param("quantity") int quantity);
    
    // Các phương thức tùy chỉnh có thể được thêm vào đây nếu cần
}
