-- Tạo function mới chỉ cập nhật updated_at cho bảng products
CREATE OR REPLACE FUNCTION update_products_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Xóa trigger cũ nếu tồn tại
DROP TRIGGER IF EXISTS update_products_updated_at_trigger ON bookstore.products;

-- Tạo trigger mới chỉ áp dụng cho bảng products
CREATE TRIGGER update_products_updated_at_trigger
BEFORE UPDATE ON bookstore.products
FOR EACH ROW
EXECUTE FUNCTION update_products_updated_at();