package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class BookRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Author is required")
    private String author;

    @Min(value = 1000, message = "Publish year must be valid")
    @Max(value = 2100, message = "Publish year must be valid")
    private Integer publishYear;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 1, message = "Pages must be at least 1")
    private Integer pages;

    private String language;
    private String publisher;
    private String isbn;
    private String imageUrl;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private Set<Long> categoryIds;
}
