package com.example.demo.exception.promotion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FlashSaleException extends RuntimeException {

    public FlashSaleException(String message) {
        super(message);
    }

    public FlashSaleException(String message, Throwable cause) {
        super(message, cause);
    }
}
