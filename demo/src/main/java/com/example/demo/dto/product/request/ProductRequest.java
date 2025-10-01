package com.example.demo.dto.product.request;

import com.example.demo.model.product.ProductType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;

    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity = 0;

    @NotNull(message = "Loại sản phẩm không được để trống")
    private ProductType type;

    @NotNull(message = "Danh sách danh mục không được để trống")
    @Size(min = 1, message = "Sản phẩm phải thuộc ít nhất một danh mục")
    private Set<Long> categoryIds;

    // Common fields
    @Size(max = 100, message = "Tác giả không được vượt quá 100 ký tự")
    private String author;

    @Size(max = 100, message = "Nhà xuất bản không được vượt quá 100 ký tự")
    private String publisher;

    @Size(max = 20, message = "ISBN không được vượt quá 20 ký tự")
    private String isbn;

    @Size(max = 50, message = "Thể loại không được vượt quá 50 ký tự")
    private String genre;

    @Min(value = 1, message = "Số trang phải lớn hơn 0")
    private Integer pageCount;

    @Size(max = 50, message = "Kích thước không được vượt quá 50 ký tự")
    private String dimensions;

    @Min(value = 0, message = "Trọng lượng phải lớn hơn hoặc bằng 0")
    private Integer weightGrams;

    private String additionalInfo;


}
