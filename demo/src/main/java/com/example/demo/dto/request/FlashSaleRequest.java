package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class FlashSaleRequest {
    @NotBlank(message = "Tên flash sale không được để trống")
    @Size(max = 255, message = "Tên flash sale không được vượt quá 255 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @Future(message = "Thời gian bắt đầu phải là thời gian trong tương lai")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @Future(message = "Thời gian kết thúc phải là thời gian trong tương lai")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Boolean isActive = true;

    private List<FlashSaleItemRequest> items = new ArrayList<>();

    @Data
    public static class FlashSaleItemRequest {
        @NotNull(message = "ID sản phẩm không được để trống")
        private Long productId;

        @NotNull(message = "Giá khuyến mãi không được để trống")
        private BigDecimal salePrice;

        @NotNull(message = "Phần trăm giảm giá không được để trống")
        @Min(value = 1, message = "Phần trăm giảm giá phải lớn hơn 0")
        @Max(value = 100, message = "Phần trăm giảm giá không được vượt quá 100%")
        private Integer discountPercent;

        @NotNull(message = "Số lượng sản phẩm không được để trống")
        @Min(value = 1, message = "Số lượng sản phẩm phải lớn hơn 0")
        private Integer quantity;

        private Boolean isActive = true;
    }
}
