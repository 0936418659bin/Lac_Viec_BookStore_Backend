-- Tạo bảng flash_sales
CREATE TABLE IF NOT EXISTS flash_sales (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_flash_sale_time CHECK (end_time > start_time)
);

-- Tạo bảng flash_sale_items
CREATE TABLE IF NOT EXISTS flash_sale_items (
    id SERIAL PRIMARY KEY,
    flash_sale_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    discount_price NUMERIC(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    sold_quantity INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_flash_sale_item_flash_sale FOREIGN KEY (flash_sale_id)
        REFERENCES flash_sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_flash_sale_item_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_flash_sale_item_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_flash_sale_item_sold_quantity CHECK (sold_quantity >= 0 AND sold_quantity <= quantity),
    CONSTRAINT uq_flash_sale_product UNIQUE (flash_sale_id, product_id)
);

-- Cập nhật bảng cart_items để hỗ trợ flash sale
ALTER TABLE cart_items
    ADD COLUMN IF NOT EXISTS price_at_addition NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS is_flash_sale BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS flash_sale_item_id INTEGER,
    ADD COLUMN IF NOT EXISTS original_price NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(10,2),
    ADD CONSTRAINT fk_cart_item_flash_sale_item FOREIGN KEY (flash_sale_item_id)
        REFERENCES flash_sale_items(id) ON DELETE SET NULL;

-- Cập nhật giá trị mặc định cho price_at_addition
UPDATE cart_items
SET
    price_at_addition = price,
    original_price = price,
    discount_amount = 0
WHERE price_at_addition IS NULL;

-- Đặt NOT NULL cho các cột bắt buộc
ALTER TABLE cart_items
    ALTER COLUMN price_at_addition SET NOT NULL,
    ALTER COLUMN is_flash_sale SET NOT NULL;

-- Tạo các index để tối ưu hiệu năng truy vấn
CREATE INDEX IF NOT EXISTS idx_flash_sales_active ON flash_sales(is_active);
CREATE INDEX IF NOT EXISTS idx_flash_sales_time_range ON flash_sales(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_flash_sale_items_flash_sale ON flash_sale_items(flash_sale_id);
CREATE INDEX IF NOT EXISTS idx_flash_sale_items_product ON flash_sale_items(product_id);

-- Thêm comment cho các bảng và cột mới
COMMENT ON TABLE flash_sales IS 'Lưu trữ thông tin các đợt flash sale';
COMMENT ON COLUMN flash_sales.is_active IS 'Trạng thái kích hoạt của flash sale';
COMMENT ON COLUMN flash_sale_items.discount_price IS 'Giá khuyến mãi trong đợt flash sale';
COMMENT ON COLUMN flash_sale_items.quantity IS 'Số lượng sản phẩm được áp dụng khuyến mãi';
COMMENT ON COLUMN flash_sale_items.sold_quantity IS 'Số lượng đã bán trong đợt flash sale';

-- Thêm comment cho các cột mới trong cart_items
COMMENT ON COLUMN cart_items.is_flash_sale IS 'Xác định xem sản phẩm có phải là flash sale không';
COMMENT ON COLUMN cart_items.price_at_addition IS 'Giá của sản phẩm tại thời điểm thêm vào giỏ hàng';
COMMENT ON COLUMN cart_items.flash_sale_item_id IS 'Tham chiếu đến flash_sale_items nếu là sản phẩm flash sale';
