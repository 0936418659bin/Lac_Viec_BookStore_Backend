package com.example.demo.dto.product.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;
    
    private String description;

    private Long parentId; // ID của danh mục cha (null nếu là danh mục cấp 1)
}
