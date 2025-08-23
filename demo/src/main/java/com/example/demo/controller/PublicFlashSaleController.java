package com.example.demo.controller;

import com.example.demo.dto.FlashSaleDTO;
import com.example.demo.dto.FlashSaleItemDTO;
import com.example.demo.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/flash-sales")
public class PublicFlashSaleController {

    private final FlashSaleService flashSaleService;

    /**
     * Lấy danh sách các đợt flash sale đang diễn ra
     */
    @GetMapping("/active")
    public ResponseEntity<List<FlashSaleDTO>> getActiveFlashSales() {
        List<FlashSaleDTO> activeFlashSales = flashSaleService.getActiveFlashSales();
        return ResponseEntity.ok(activeFlashSales);
    }

    /**
     * Lấy danh sách các đợt flash sale sắp diễn ra
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<FlashSaleDTO>> getUpcomingFlashSales() {
        List<FlashSaleDTO> upcomingFlashSales = flashSaleService.getUpcomingFlashSales();
        return ResponseEntity.ok(upcomingFlashSales);
    }

    /**
     * Lấy thông tin chi tiết một đợt flash sale
     */
    @GetMapping("/{id}")
    public ResponseEntity<FlashSaleDTO> getFlashSaleById(@PathVariable Long id) {
        FlashSaleDTO flashSale = flashSaleService.getFlashSaleById(id);
        return ResponseEntity.ok(flashSale);
    }

    /**
     * Lấy danh sách sản phẩm đang được flash sale
     */
    @GetMapping("/items/active")
    public ResponseEntity<Page<FlashSaleItemDTO>> getActiveFlashSaleItems(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<FlashSaleItemDTO> activeItems = flashSaleService.getActiveFlashSaleItems(pageable);
        return ResponseEntity.ok(activeItems);
    }

    /**
     * Kiểm tra sản phẩm có đang được flash sale không
     */
    @GetMapping("/items/check-product/{productId}")
    public ResponseEntity<FlashSaleItemDTO> checkProductInFlashSale(@PathVariable Long productId) {
        FlashSaleItemDTO item = flashSaleService.checkProductInFlashSale(productId);
        if (item != null) {
            return ResponseEntity.ok(item);
        }
        return ResponseEntity.noContent().build();
    }
}
