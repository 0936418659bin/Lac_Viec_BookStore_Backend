package com.example.demo.dto.product.response;

import com.example.demo.model.product.ProductStatus;
import com.example.demo.model.product.ProductType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private ProductType type;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double averageRating;
    private Integer reviewCount;
    private Boolean isFeatured;
    private Boolean isNewArrival;
    private Boolean isOnSale;
    private BigDecimal salePrice;
    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;

    // Book specific fields
    private String author;
    private String publisher;
    private String isbn;
    private String genre;
    private Integer pageCount;
    private String dimensions;
    private Integer weightGrams;
    private String additionalInfo;

    // Relationships
    private Set<CategoryResponse> categories;
    private List<String> imageUrls;

    // Additional computed fields
    private Boolean inStock;
    private Boolean onSale;

    public Boolean getInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public Boolean getOnSale() {
        if (salePrice == null || saleStartDate == null || saleEndDate == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(saleStartDate) && now.isBefore(saleEndDate);
    }
}
