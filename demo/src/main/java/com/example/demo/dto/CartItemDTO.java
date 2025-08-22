package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private int quantity;
    private BigDecimal subTotal;
    private boolean isFlashSale;
    private BigDecimal originalPrice; // Giá gốc trước khi giảm giá (nếu có)
    private BigDecimal discountAmount; // Số tiền được giảm (nếu có)
}
