package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class BookRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Author is required")
    private String author;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 1, message = "Page count must be at least 1")
    private Integer pageCount;

    private String language;
    private String publisher;
    private String isbn;
    private String imageUrl; // Giữ lại cho tương thích ngược
    private List<String> imageUrls; // Thêm trường mới cho nhiều ảnh

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private String genre;
    private LocalDate publicationDate;
    private String dimensions;
    private Integer weightGrams;
    private String additionalInfo;
    private Set<Long> categoryIds;

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
