package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlashSaleItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer discountPercent;
    private Integer quantity;
    private Integer soldQuantity;
    private Integer remainingQuantity;
    private Boolean active;
    private Integer maxQuantityPerUser;
    private Long flashSaleId;
}
