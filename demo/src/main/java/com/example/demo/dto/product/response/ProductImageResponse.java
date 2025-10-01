package com.example.demo.dto.product.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    private Integer sortOrder;
    private Boolean isThumbnail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
