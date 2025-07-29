package com.example.demo.dto.response;

import com.example.demo.model.Category;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class BookResponse {
    private Long id;
    private String title;
    private String description;
    private String author;
    private Integer publishYear;
    private BigDecimal price;
    private Integer pages;
    private String language;
    private String publisher;
    private String isbn;
    private String imageUrl;
    private Integer stockQuantity;
    private Set<String> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookResponse fromBook(com.example.demo.model.Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setDescription(book.getDescription());
        response.setAuthor(book.getAuthor());
        response.setPublishYear(book.getPublishYear());
        response.setPrice(book.getPrice());
        response.setPages(book.getPages());
        response.setLanguage(book.getLanguage());
        response.setPublisher(book.getPublisher());
        response.setIsbn(book.getIsbn());
        response.setImageUrl(book.getImageUrl());
        response.setStockQuantity(book.getStockQuantity());
        response.setCategories(book.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toSet()));
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());
        return response;
    }
}
