package com.example.demo.controller;

import com.example.demo.dto.AddToCartRequest;
import com.example.demo.dto.CartItemDTO;
import com.example.demo.dto.CartSummaryDTO;
import com.example.demo.dto.FlashSaleItemDTO;
import com.example.demo.service.CartService;
import com.example.demo.service.FlashSaleService;
import com.example.demo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Giỏ hàng", description = "Quản lý giỏ hàng và thanh toán")
public class CartController {

    private final CartService cartService;
    private final FlashSaleService flashSaleService;
    private final ProductService productService;
    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy danh sách sản phẩm trong giỏ hàng")
    public ResponseEntity<List<CartItemDTO>> getCartItems() {
        List<CartItemDTO> cartItems = cartService.getCartItems();
        return ResponseEntity.ok(cartItems);
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy thông tin tổng quan giỏ hàng")
    public ResponseEntity<CartSummaryDTO> getCartSummary() {
        CartSummaryDTO summary = cartService.getCartSummary();
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng")
    @ApiResponse(responseCode = "200", description = "Thêm sản phẩm vào giỏ hàng thành công")
    @ApiResponse(responseCode = "400", description = "Số lượng không hợp lệ hoặc không đủ hàng")
    public ResponseEntity<?> addToCart(
            @Parameter(description = "Thông tin sản phẩm cần thêm vào giỏ hàng")
            @Valid @RequestBody AddToCartRequest request) {
        try {
            // If this is a flash sale item, validate it first
            if (request.getIsFlashSaleItem()) {
                FlashSaleItemDTO flashSaleItem = flashSaleService.checkProductInFlashSale(request.getProductId());
                if (flashSaleItem == null || !flashSaleItem.getActive()) {
                    return ResponseEntity.badRequest().body("Sản phẩm này không còn trong đợt flash sale");
                }
                request.setPrice(flashSaleItem.getSalePrice());
            }

            CartItemDTO cartItem = cartService.addToCart(request);
            return ResponseEntity.ok(cartItem);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi thêm sản phẩm vào giỏ hàng", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi thêm sản phẩm vào giỏ hàng");
        }
    }

    @PutMapping("/update-quantity/{itemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ hàng")
    public ResponseEntity<?> updateCartItemQuantity(
            @Parameter(description = "ID của sản phẩm trong giỏ hàng")
            @PathVariable Long itemId,
            @Parameter(description = "Số lượng mới")
            @RequestParam int quantity) {
        try {
            if (quantity <= 0) {
                return ResponseEntity.badRequest().body("Số lượng phải lớn hơn 0");
            }
            
            CartItemDTO updatedItem = cartService.updateCartItemQuantity(itemId, quantity);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra khi cập nhật giỏ hàng");
        }
    }

    @DeleteMapping("/remove/{itemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng")
    public ResponseEntity<?> removeFromCart(
            @Parameter(description = "ID của sản phẩm cần xóa")
            @PathVariable Long itemId) {
        try {
            cartService.removeFromCart(itemId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra khi xóa sản phẩm");
        }
    }

    @DeleteMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa toàn bộ giỏ hàng")
    public ResponseEntity<?> clearCart() {
        try {
            cartService.clearCart();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra khi xóa giỏ hàng");
        }
    }

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thanh toán giỏ hàng")
    public ResponseEntity<?> checkout() {
        try {
            cartService.checkout();
            return ResponseEntity.ok().body("Thanh toán thành công");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi thanh toán: " + e.getMessage());
        }
    }
}
