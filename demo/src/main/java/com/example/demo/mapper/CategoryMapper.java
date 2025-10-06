package com.example.demo.mapper;

import com.example.demo.dto.product.response.CategoryResponse;
import com.example.demo.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "hasChildren", expression = "java(category.getChildren() != null && !category.getChildren().isEmpty())")
    @Mapping(target = "children", source = "children")
    CategoryResponse toResponse(Category category);

    default Set<CategoryResponse> toResponseSet(Set<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }

    default List<CategoryResponse> toResponseList(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
