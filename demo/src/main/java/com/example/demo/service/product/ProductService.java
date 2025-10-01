package com.example.demo.service.product;

import com.example.demo.dto.product.request.BaseProductRequest;
import com.example.demo.dto.product.response.BaseProductResponse;
import com.example.demo.model.product.Product;
import com.example.demo.model.product.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface chung cho tất cả các dịch vụ sản phẩm
 * @param <T> Loại Product
 * @param <R> Loại Response DTO
 */
public interface ProductService<T extends Product, R extends BaseProductResponse> {

    /**
     * Tạo mới sản phẩm
     */
    R create(BaseProductRequest request);

    /**
     * Cập nhật thông tin sản phẩm
     */
    R update(Long id, BaseProductRequest request);

    /**
     * Xóa sản phẩm
     */
    void delete(Long id);

    /**
     * Lấy thông tin chi tiết sản phẩm
     */
    R getById(Long id);

    /**
     * Tìm kiếm sản phẩm
     */
    Page<R> search(String keyword, List<Long> categoryIds, Pageable pageable);

    /**
     * Lấy tất cả sản phẩm
     */
    Page<R> getAllProducts(Pageable pageable);

    /**
     * Cập nhật số lượng sản phẩm
     */
    void updateProductStock(Long productId, int quantity);

    /**
     * Tìm kiếm sản phẩm mở rộng
     */
    Page<R> searchProducts(String keyword, ProductType type, Long categoryId, Pageable pageable);

    /**
     * Lấy loại sản phẩm mà service này xử lý
     */
    ProductType getProductType();

    /**
     * Kiểm tra xem service có hỗ trợ loại sản phẩm này không
     */
    boolean supports(ProductType type);

    /**
     * Tìm kiếm sản phẩm theo id và trả về đối tượng Product thực tế
     */
    Product getProductEntityById(Long id);

}