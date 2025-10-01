package com.example.demo.dto.product.response;

import com.example.demo.model.product.ProductStatus;
import com.example.demo.model.product.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private ProductStatus status;
    private ProductType type;
    private Set<Long> categoryIds;
    private List<String> imageUrls;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}