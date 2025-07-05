package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    private String debugMessage;

    public ErrorResponse(HttpStatus status, String message, Throwable ex) {
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.debugMessage = ex.getLocalizedMessage();
    }

    public ErrorResponse(HttpStatus status, String message) {
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }
}
