package com.example.demo.dto.order.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddToCartRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Giá sản phẩm (có thể là giá gốc hoặc giá flash sale)
    private BigDecimal price;

    // Cờ đánh dấu có phải là sản phẩm flash sale không
    private Boolean isFlashSaleItem = Boolean.FALSE;
}
