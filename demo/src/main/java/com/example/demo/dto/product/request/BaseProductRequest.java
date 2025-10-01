package com.example.demo.dto.product.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public abstract class BaseProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity = 0;

    @NotEmpty(message = "Vui lòng chọn ít nhất một danh mục")
    private Set<Long> categoryIds;

    @NotEmpty(message = "Vui lòng cung cấp ít nhất một ảnh")
    @Size(max = 5, message = "Chỉ được tối đa 5 ảnh")
    private List<String> imageUrls;

    private Boolean isFeatured = false;
}