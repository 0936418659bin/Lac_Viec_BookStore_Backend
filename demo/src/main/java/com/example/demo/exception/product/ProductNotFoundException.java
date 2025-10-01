package com.example.demo.exception.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super(String.format("Không tìm thấy sản phẩm với ID: %s", id));
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
