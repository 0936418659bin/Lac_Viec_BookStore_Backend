package com.example.demo.dto.product.response;

import com.example.demo.model.Category;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
public class BookResponse {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private List<String> imageUrls;
    private Integer stockQuantity;
    private String publisher;
    private String genre;
    private Integer pageCount;
    private LocalDate publicationDate;
    private String dimensions;
    private Integer weightGrams;
    private Object additionalInfo;
    private Set<String> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookResponse fromBook(com.example.demo.model.Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setIsbn(book.getIsbn());
        response.setDescription(book.getDescription());
        response.setPrice(book.getPrice());
        
        // Xử lý ảnh
        List<String> allImageUrls = book.getAllImageUrls();
        response.setImageUrls(allImageUrls);
        response.setImageUrl(!allImageUrls.isEmpty() ? allImageUrls.get(0) : null);
        
        response.setStockQuantity(book.getStockQuantity());
        response.setPublisher(book.getPublisher());
        response.setGenre(book.getGenre());
        response.setPageCount(book.getPageCount());
        response.setPublicationDate(book.getPublicationDate());
        response.setDimensions(book.getDimensions());
        response.setWeightGrams(book.getWeightGrams());
        
        // Xử lý additionalInfo
        if (book.getAdditionalInfo() != null && !book.getAdditionalInfo().trim().isEmpty()) {
            try {
                // Parse lại JSON để đảm bảo định dạng đúng
                response.setAdditionalInfo(objectMapper.readValue(book.getAdditionalInfo(), Object.class));
            } catch (Exception e) {
                log.warn("Failed to parse additionalInfo as JSON: {}", book.getAdditionalInfo());
                response.setAdditionalInfo(book.getAdditionalInfo());
            }
        }
        
        // Lấy danh sách category names
        response.setCategories(book.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toSet()));
                
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());
        return response;
    }
}
