package com.example.demo.model.product;

/**
 * Enum đại diện cho các trạng thái của sản phẩm
 */
public enum ProductStatus {
    /**
     * Sản phẩm đang hoạt động và có sẵn để bán
     */
    ACTIVE,

    /**
     * Sản phẩm tạm ngừng kinh doanh
     */
    INACTIVE,

    /**
     * Sản phẩm đã hết hàng
     */
    OUT_OF_STOCK,

    /**
     * Sản phẩm đang được khuyến mãi
     */
    ON_SALE,

    /**
     * Sản phẩm mới về
     */
    NEW_ARRIVAL,

    /**
     * Sản phẩm đã ngừng kinh doanh
     */
    DISCONTINUED,

    /**
     * Sản phẩm đang chờ duyệt
     */
    PENDING_REVIEW,

    /**
     * Sản phẩm đã bị từ chối
     */
    REJECTED
}
