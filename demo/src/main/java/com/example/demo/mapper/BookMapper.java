package com.example.demo.mapper;

import com.example.demo.dto.product.request.BookRequest;
import com.example.demo.dto.product.response.BookResponse;
import com.example.demo.model.Category;
import com.example.demo.model.product.Book;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CategoryMapper.class})
public abstract class BookMapper implements ProductMapper<Book, BookRequest, BookResponse> {

    @Autowired
    protected CategoryMapper categoryMapper;

    @Override
    public Book toEntity(BookRequest request) {
        if (request == null) {
            return null;
        }

        Book book = new Book();
        book.setName(request.getName());
        book.setDescription(request.getDescription());
        book.setPrice(request.getPrice());
        book.setStockQuantity(request.getStockQuantity());
        book.setIsFeatured(request.getIsFeatured());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setIsbn(request.getIsbn());
        book.setGenre(request.getGenre());
        book.setPageCount(request.getPageCount());
        book.setPublicationDate(request.getPublicationDate());
        book.setDimensions(request.getDimensions());
        book.setLanguage(request.getLanguage());
        book.setWeightGrams(request.getWeightGrams());
        book.setAdditionalInfo(request.getAdditionalInfo());

        // Set image URL (assuming single image for now)
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            book.setImageUrl(request.getImageUrls().get(0));
        }

        return book;
    }

    @Override
    public BookResponse toResponse(Book book) {
        if (book == null) {
            return null;
        }

        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setName(book.getName());
        response.setDescription(book.getDescription());
        response.setPrice(book.getPrice());
        response.setStockQuantity(book.getStockQuantity());
        response.setIsFeatured(book.getIsFeatured());
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());
        response.setStatus(book.getStatus());
        response.setType(book.getType());

        // Set image URLs (as a list with single item)
        if (book.getImageUrl() != null) {
            response.setImageUrls(java.util.Collections.singletonList(book.getImageUrl()));
        }

        // Map categories
        if (book.getCategories() != null) {
            response.setCategoryIds(book.getCategories().stream()
                    .map(Category::getId)
                    .collect(Collectors.toSet()));
        }

        // Book specific fields
        response.setAuthor(book.getAuthor());
        response.setPublisher(book.getPublisher());
        response.setIsbn(book.getIsbn());
        response.setGenre(book.getGenre());
        response.setPageCount(book.getPageCount());
        response.setPublicationDate(book.getPublicationDate());
        response.setDimensions(book.getDimensions());
        response.setLanguage(book.getLanguage());
        response.setWeightGrams(book.getWeightGrams());
        response.setAdditionalInfo(book.getAdditionalInfo());

        return response;
    }

    @Override
    public void updateFromRequest(BookRequest request, @MappingTarget Book entity) {
        if (request == null || entity == null) {
            return;
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            entity.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            entity.setStockQuantity(request.getStockQuantity());
        }
        if (request.getIsFeatured() != null) {
            entity.setIsFeatured(request.getIsFeatured());
        }
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            entity.setImageUrl(request.getImageUrls().get(0));
        }

        // Update book specific fields
        if (request.getAuthor() != null) {
            entity.setAuthor(request.getAuthor());
        }
        if (request.getPublisher() != null) {
            entity.setPublisher(request.getPublisher());
        }
        if (request.getIsbn() != null) {
            entity.setIsbn(request.getIsbn());
        }
        if (request.getGenre() != null) {
            entity.setGenre(request.getGenre());
        }
        if (request.getPageCount() != null) {
            entity.setPageCount(request.getPageCount());
        }
        if (request.getPublicationDate() != null) {
            entity.setPublicationDate(request.getPublicationDate());
        }
        if (request.getDimensions() != null) {
            entity.setDimensions(request.getDimensions());
        }
        if (request.getLanguage() != null) {
            entity.setLanguage(request.getLanguage());
        }
        if (request.getWeightGrams() != null) {
            entity.setWeightGrams(request.getWeightGrams());
        }
        if (request.getAdditionalInfo() != null) {
            entity.setAdditionalInfo(request.getAdditionalInfo());
        }
    }

    @Named("mapCategoryIds")
    public Set<Category> mapCategoryIds(Set<Long> categoryIds) {
        if (categoryIds == null) {
            return null;
        }
        return categoryIds.stream()
                .map(id -> {
                    Category category = new Category();
                    category.setId(id);
                    return category;
                })
                .collect(Collectors.toSet());
    }

    @Named("mapCategoryIds")
    public Set<Long> mapCategories(Set<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
    }
}