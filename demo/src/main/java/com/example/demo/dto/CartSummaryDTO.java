package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartSummaryDTO {
    private int totalItems;
    private int totalProducts;
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal total;
    private List<CartItemDTO> items;

    public CartSummaryDTO(List<CartItemDTO> items) {
        this.items = items;
        this.totalItems = items.stream().mapToInt(CartItemDTO::getQuantity).sum();
        this.totalProducts = items.size();
        this.subtotal = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.discountTotal = items.stream()
                .filter(CartItemDTO::isFlashSale)
                .map(item -> item.getOriginalPrice().subtract(item.getPrice())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.total = this.subtotal;
    }
}
