package com.example.demo.model.product;

import com.example.demo.model.Category;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicUpdate
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "products", schema = "bookstore")
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_flash_sale")
    private Boolean isFlashSale = false;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToMany
    @JoinTable(
        name = "product_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> images = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Các phương thức tiện ích
    public void addCategory(Category category) {
        this.categories.add(category);
        category.getProducts().add(this);
    }

    public void removeCategory(Category category) {
        this.categories.remove(category);
        category.getProducts().remove(this);
    }

    public Set<ProductImage> getImages() {
        return images;
    }
}
