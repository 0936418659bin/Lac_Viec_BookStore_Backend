package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Entity
@DynamicUpdate
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "products", schema = "bookstore")
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "stock")
    private Integer stockQuantity = Integer.valueOf(0);

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private ProductType type;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "active")
    private Boolean active = Boolean.TRUE;

    @Column(name = "is_flash_sale")
    private Boolean isFlashSale = Boolean.FALSE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_categories",
        schema = "bookstore",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @JsonIgnore
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ProductImage> images = new ArrayList<>();

    // Các phương thức tiện ích
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    // Lấy URL ảnh đại diện (thumbnail)
    @Transient
    public String getImageUrl() {
        if (this.images == null || this.images.isEmpty()) {
            return null;
        }
        
        // Tìm ảnh đại diện (thumbnail) đầu tiên
        return this.images.stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElseGet(() -> this.images.get(0).getImageUrl());
    }

    // Lấy danh sách URL của tất cả ảnh
    @Transient
    public List<String> getAllImageUrls() {
        if (this.images == null || this.images.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Sử dụng LinkedHashSet để loại bỏ các URL trùng lặp nhưng vẫn giữ nguyên thứ tự
        Set<String> uniqueUrls = new LinkedHashSet<>();
        for (ProductImage image : this.images) {
            if (image != null && image.getImageUrl() != null) {
                uniqueUrls.add(image.getImageUrl().trim());
            }
        }
        
        return new ArrayList<>(uniqueUrls);
    }

    // Thêm ảnh mới cho sản phẩm
    public void addImage(String imageUrl, boolean isThumbnail) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        
        // Nếu đặt làm ảnh đại diện, bỏ đặt tất cả ảnh khác
        if (isThumbnail) {
            this.images.forEach(img -> img.setThumbnail(false));
        }
        
        ProductImage image = new ProductImage();
        image.setImageUrl(imageUrl);
        image.setThumbnail(isThumbnail);
        image.setProduct(this);
        this.images.add(image);
    }
}
