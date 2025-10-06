package com.example.demo.mapper;

import com.example.demo.dto.product.request.BaseProductRequest;
import com.example.demo.dto.product.response.BaseProductResponse;
import com.example.demo.model.Category;
import com.example.demo.model.product.Product;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Set;

/**
 * Interface for mapping between Product entities and DTOs
 * @param <T> Entity type that extends Product
 * @param <R> Request DTO type that extends BaseProductRequest
 * @param <S> Response DTO type that extends BaseProductResponse
 */
public interface ProductMapper<T extends Product, R extends BaseProductRequest, S extends BaseProductResponse> {
    
    /**
     * Convert Request DTO to Entity
     */
    T toEntity(R request);
    
    /**
     * Convert Entity to Response DTO
     */
    S toResponse(T entity);
    
    /**
     * Update Entity from Request DTO
     */
    void updateFromRequest(R request, @MappingTarget T entity);
    
    /**
     * Map category IDs to Category entities
     */
    @Named("mapCategories")
    default Set<Category> mapCategoryIds(Set<Long> categoryIds) {
        return null; // Implementation should be provided by concrete mappers
    }
    
    /**
     * Map Category entities to their IDs
     */
    @Named("mapCategoryIds")
    default Set<Long> mapCategories(Set<Category> categories) {
        return null; // Implementation should be provided by concrete mappers
    }
}