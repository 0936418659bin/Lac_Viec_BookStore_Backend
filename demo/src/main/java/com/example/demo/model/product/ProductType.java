package com.example.demo.model.product;

import lombok.Getter;

@Getter
public enum ProductType {
    BOOK("Sách"),
    RULE("Thước kẻ"),
    BAG("Cặp sách"),
    PEN("Bút"),
    NOTEBOOK("Vở"),
    OTHER("Khác");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }
}
