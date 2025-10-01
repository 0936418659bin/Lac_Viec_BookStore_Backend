package com.example.demo.dto.product.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProductImageRequest {
    @NotEmpty(message = "Danh sách file không được để trống")
    private List<MultipartFile> files;
}
