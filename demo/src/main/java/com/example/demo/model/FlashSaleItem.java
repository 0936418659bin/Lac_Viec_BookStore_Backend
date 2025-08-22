package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "flash_sale_items")
public class FlashSaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    private FlashSale flashSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sale_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;  // Changed from salePrice to price

    @Column(name = "discount_price", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPrice ;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "sold_quantity")
    private Integer soldQuantity = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Getter for available quantity
    public Integer getAvailableQuantity() {
        return this.quantity - this.soldQuantity;
    }

    // Alias getter for salePrice to maintain backward compatibility
    public BigDecimal getSalePrice() {
        return this.price;
    }
}