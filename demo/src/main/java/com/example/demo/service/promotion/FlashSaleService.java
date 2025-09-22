package com.example.demo.service.promotion;

import com.example.demo.dto.promotion.response.FlashSaleDTO;
import com.example.demo.dto.promotion.response.FlashSaleItemDTO;
import com.example.demo.dto.promotion.request.FlashSaleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FlashSaleService {
    
    /**
     * Lấy danh sách sản phẩm đang được flash sale
     */
    Page<FlashSaleItemDTO> getActiveFlashSaleItems(Pageable pageable);

    /**
     * Kiểm tra sản phẩm có đang được flash sale không
     */
    FlashSaleItemDTO checkProductInFlashSale(Long productId);
    
    /**
     * Tạo mới một đợt flash sale
     */
    FlashSaleDTO createFlashSale(FlashSaleRequest request);
    
    /**
     * Cập nhật thông tin đợt flash sale
     */
    FlashSaleDTO updateFlashSale(Long id, FlashSaleRequest request);
    
    /**
     * Lấy thông tin chi tiết đợt flash sale theo ID
     */
    FlashSaleDTO getFlashSaleById(Long id);
    
    /**
     * Lấy danh sách tất cả đợt flash sale (có phân trang)
     */
    Page<FlashSaleDTO> getAllFlashSales(Pageable pageable);
    
    /**
     * Xóa một đợt flash sale
     */
    void deleteFlashSale(Long id);
    
    /**
     * Lấy danh sách các đợt flash sale đang diễn ra
     */
    List<FlashSaleDTO> getActiveFlashSales();
    
    /**
     * Lấy danh sách các đợt flash sale sắp diễn ra
     */
    List<FlashSaleDTO> getUpcomingFlashSales();
    
    /**
     * Thêm sản phẩm vào đợt flash sale
     */
    FlashSaleItemDTO addItemToFlashSale(Long flashSaleId, FlashSaleRequest.FlashSaleItemRequest itemRequest);
    
    /**
     * Cập nhật thông tin sản phẩm trong đợt flash sale
     */
    FlashSaleItemDTO updateFlashSaleItem(Long flashSaleId, Long itemId, FlashSaleRequest.FlashSaleItemRequest itemRequest);
    
    /**
     * Xóa sản phẩm khỏi đợt flash sale
     */
    void removeItemFromFlashSale(Long flashSaleId, Long itemId);
    
    /**
     * Lấy số lượng sản phẩm còn lại trong flash sale
     */
    int getRemainingQuantity(Long productId);
    
    /**
     * Thêm sản phẩm vào flash sale
     */
    void addToFlashSale(Long productId, int quantity);
    
    /**
     * Xóa sản phẩm khỏi flash sale
     */
    void removeFromFlashSale(Long productId);
    
    /**
     * Xử lý mua hàng flash sale
     */
    boolean purchaseFlashSale(Long productId, int quantity);
    
    /**
     * Cập nhật số lượng đã bán của sản phẩm
     */
    void updateSoldQuantity(Long productId, int quantity);
    
    /**
     * Cập nhật trạng thái đợt flash sale
     */
    void updateFlashSaleStatus();
}
