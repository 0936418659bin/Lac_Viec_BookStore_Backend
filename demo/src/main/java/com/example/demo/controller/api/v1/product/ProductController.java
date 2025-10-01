package com.example.demo.controller.api.v1.product;

import com.example.demo.dto.product.request.BaseProductRequest;
import com.example.demo.dto.product.response.BaseProductResponse;
import com.example.demo.model.product.ProductType;
import com.example.demo.service.product.ProductImageService;
import com.example.demo.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController<R extends BaseProductResponse> {

    private final ProductService<?, R> productService;
    private final ProductImageService productImageService;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<R> createProduct(
            @Valid @RequestBody BaseProductRequest request) {
        R response = productService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<R> updateProduct(
            @Parameter(description = "ID of the product to update") @PathVariable Long id,
            @Valid @RequestBody BaseProductRequest request) {
        R response = productService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID")
    public ResponseEntity<R> getProductById(
            @Parameter(description = "ID of the product to retrieve") @PathVariable Long id) {
        R response = productService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<Page<R>> getAllProducts(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<R> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products with filters")
    public ResponseEntity<Page<R>> searchProducts(
            @Parameter(description = "Keyword to search in name or description")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Product type to filter by")
            @RequestParam(required = false) String type,

            @Parameter(description = "Category ID to filter by")
            @RequestParam(required = false) Long categoryId,

            @PageableDefault(size = 20) Pageable pageable) {

        ProductType productType = null;
        if (type != null && !type.isEmpty()) {
            try {
                productType = ProductType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product type");
            }
        }
        
        Page<R> response = productService.searchProducts(keyword, productType, categoryId, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product by ID")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID of the product to delete") @PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải lên hình ảnh cho sản phẩm")
    public ResponseEntity<List<String>> uploadProductImages(
            @Parameter(description = "ID của sản phẩm") @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        
        List<String> imageUrls = productImageService.uploadImages(id, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(imageUrls);
    }

    @DeleteMapping("/{productId}/images/{imageUrl}")
    @Operation(summary = "Xóa hình ảnh của sản phẩm")
    public ResponseEntity<Void> deleteProductImage(
            @Parameter(description = "ID của sản phẩm") @PathVariable Long productId,
            @Parameter(description = "URL của hình ảnh cần xóa") @PathVariable String imageUrl) {
        
        productImageService.deleteImage(productId, imageUrl);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "Lấy danh sách hình ảnh của sản phẩm")
    public ResponseEntity<List<String>> getProductImages(
            @Parameter(description = "ID của sản phẩm") @PathVariable Long id) {
        
        List<String> imageUrls = productImageService.getImageUrlsByProductId(id);
        return ResponseEntity.ok(imageUrls);
    }

    @GetMapping("/images/{filename:.+}")
    @Operation(summary = "Tải xuống hình ảnh")
    public ResponseEntity<Resource> downloadImage(
            @Parameter(description = "Tên file hình ảnh") @PathVariable String filename) {
        
        // Implementation for downloading image would go here
        // Return a Resource object with appropriate headers
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update product stock quantity")
    public ResponseEntity<Void> updateStock(
            @Parameter(description = "ID of the product") @PathVariable Long id,
            @RequestParam int quantity) {
        productService.updateProductStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Update product status")
    public ResponseEntity<Void> updateStatus(
            @Parameter(description = "ID of the product") @PathVariable Long id,
            @RequestParam String status) {
        // Implementation for updating status would go here
        return ResponseEntity.ok().build();
    }
}
