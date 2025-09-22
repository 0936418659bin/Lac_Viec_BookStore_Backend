package com.example.demo.controller.api.v1.order;

import com.example.demo.dto.promotion.response.FlashSaleDTO;
import com.example.demo.dto.promotion.response.FlashSaleItemDTO;
import com.example.demo.dto.promotion.request.FlashSaleRequest;
import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.exception.common.BadRequestException;
import com.example.demo.service.promotion.FlashSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/flash-sales")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @Operation(summary = "Create a new flash sale")
    @ApiResponse(responseCode = "201", description = "Flash sale created successfully")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlashSaleDTO> createFlashSale(
            @Valid @RequestBody FlashSaleRequest request) {
        FlashSaleDTO createdFlashSale = flashSaleService.createFlashSale(request);
        return ResponseEntity.created(URI.create("/api/flash-sales/" + createdFlashSale.getId()))
                .body(createdFlashSale);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return new ResponseEntity<>(
                new ErrorResponse("Bad Request", ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        return new ResponseEntity<>(
                new ErrorResponse("Internal Server Error", "An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlashSaleDTO> updateFlashSale(
            @PathVariable Long id,
            @Valid @RequestBody FlashSaleRequest request) {
        FlashSaleDTO updatedFlashSale = flashSaleService.updateFlashSale(id, request);
        return ResponseEntity.ok(updatedFlashSale);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlashSaleDTO> getFlashSaleById(@PathVariable Long id) {
        FlashSaleDTO flashSale = flashSaleService.getFlashSaleById(id);
        return ResponseEntity.ok(flashSale);
    }

    @GetMapping
    public ResponseEntity<Page<FlashSaleDTO>> getAllFlashSales(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<FlashSaleDTO> flashSales = flashSaleService.getAllFlashSales(pageable);
        return ResponseEntity.ok(flashSales);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFlashSale(@PathVariable Long id) {
        flashSaleService.deleteFlashSale(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<FlashSaleDTO>> getActiveFlashSales() {
        List<FlashSaleDTO> activeFlashSales = flashSaleService.getActiveFlashSales();
        return ResponseEntity.ok(activeFlashSales);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<FlashSaleDTO>> getUpcomingFlashSales() {
        List<FlashSaleDTO> upcomingFlashSales = flashSaleService.getUpcomingFlashSales();
        return ResponseEntity.ok(upcomingFlashSales);
    }

    @PostMapping("/{flashSaleId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlashSaleItemDTO> addItemToFlashSale(
            @PathVariable Long flashSaleId,
            @Valid @RequestBody FlashSaleRequest.FlashSaleItemRequest itemRequest) {
        FlashSaleItemDTO item = flashSaleService.addItemToFlashSale(flashSaleId, itemRequest);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{flashSaleId}/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlashSaleItemDTO> updateFlashSaleItem(
            @PathVariable Long flashSaleId,
            @PathVariable Long itemId,
            @Valid @RequestBody FlashSaleRequest.FlashSaleItemRequest itemRequest) {
        FlashSaleItemDTO updatedItem = flashSaleService.updateFlashSaleItem(flashSaleId, itemId, itemRequest);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{flashSaleId}/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeItemFromFlashSale(
            @PathVariable Long flashSaleId,
            @PathVariable Long itemId) {
        flashSaleService.removeItemFromFlashSale(flashSaleId, itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get active flash sale items with pagination")
    @GetMapping("/items/active")
    public ResponseEntity<Page<FlashSaleItemDTO>> getActiveFlashSaleItems(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10) Pageable pageable) {
        Page<FlashSaleItemDTO> activeItems = flashSaleService.getActiveFlashSaleItems(pageable);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(activeItems);
    }


    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponse("Not Found", ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @GetMapping("/items/check-product/{productId}")
    public ResponseEntity<FlashSaleItemDTO> checkProductInFlashSale(@PathVariable Long productId) {
        FlashSaleItemDTO item = flashSaleService.checkProductInFlashSale(productId);
        if (item != null) {
            return ResponseEntity.ok(item);
        }
        return ResponseEntity.noContent().build();
    }
}
