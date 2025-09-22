-- V1__init_schema.sql: Gộp tất cả migration từ V1 đến V8


-- Tạo schema nếu chưa tồn tại
CREATE SCHEMA IF NOT EXISTS bookstore;

-- Đặt search_path cho session hiện tại
SET search_path TO bookstore;

-- Tạo các bảng cơ bản
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    type VARCHAR(50) DEFAULT 'book',
    parent_id INTEGER REFERENCES categories(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    category_id INTEGER REFERENCES categories(id),
    description TEXT,
    stock INTEGER DEFAULT 0,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    active BOOLEAN DEFAULT true,
    is_flash_sale BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Thêm trigger cập nhật updated_at cho products
CREATE OR REPLACE FUNCTION update_products_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_products_updated_at_trigger
BEFORE UPDATE ON products
FOR EACH ROW
EXECUTE FUNCTION update_products_updated_at();

-- Các bảng khác
CREATE TABLE product_images (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(255) NOT NULL,
    is_thumbnail BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng books với đầy đủ các trường
CREATE TABLE books (
    product_id INTEGER PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    author VARCHAR(100),
    publisher VARCHAR(100),
    isbn VARCHAR(20),
    genre VARCHAR(100),
    page_count INTEGER,
    publication_date DATE,
    dimensions VARCHAR(50),
    weight_grams INTEGER,
    additional_info JSONB,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trigger cập nhật updated_at cho books
CREATE OR REPLACE FUNCTION update_books_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_books_updated_at_trigger
BEFORE UPDATE ON books
FOR EACH ROW EXECUTE FUNCTION update_books_updated_at();

-- Các bảng sản phẩm khác
CREATE TABLE pens (
    product_id INTEGER PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    brand VARCHAR(100),
    color VARCHAR(50),
    pen_type VARCHAR(50),
    material VARCHAR(100)
);

CREATE TABLE notebooks (
    product_id INTEGER PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    brand VARCHAR(100),
    size VARCHAR(50),
    page_count INTEGER,
    paper_type VARCHAR(100)
);

CREATE TABLE rulers (
    product_id INTEGER PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    brand VARCHAR(100),
    length_cm INTEGER,
    material VARCHAR(100)
);

-- Bảng trung gian product_categories
CREATE TABLE product_categories (
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES categories(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id, category_id)
);

-- Các bảng đơn hàng và thanh toán
CREATE TABLE addresses (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    address_line VARCHAR(255) NOT NULL,
    city VARCHAR(100),
    district VARCHAR(100),
    ward VARCHAR(100),
    phone VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    address_id INTEGER REFERENCES addresses(id),
    total NUMERIC(12,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES products(id),
    quantity INTEGER NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    PRIMARY KEY (order_id, product_id)
);

-- Bảng giỏ hàng với đầy đủ các trường
CREATE TABLE cart_items (
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES products(id),
    quantity INTEGER NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    price_at_addition NUMERIC(10,2) NOT NULL,
    is_flash_sale BOOLEAN DEFAULT FALSE,
    flash_sale_item_id INTEGER,
    original_price NUMERIC(10,2) NOT NULL,
    discount_amount NUMERIC(10,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, product_id)
);

-- Bảng thanh toán
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE UNIQUE,
    payment_method VARCHAR(50),
    payment_type VARCHAR(50),
    payment_status VARCHAR(50),
    transaction_id VARCHAR(100),
    amount NUMERIC(12,2),
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng flash sale
CREATE TABLE flash_sales (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'UPCOMING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_flash_sale_time CHECK (end_time > start_time)
);

CREATE TABLE flash_sale_items (
    id SERIAL PRIMARY KEY,
    flash_sale_id INTEGER NOT NULL REFERENCES flash_sales(id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    discount_percent NUMERIC(5,2) NOT NULL,
    discount_amount NUMERIC(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    sold_quantity INTEGER DEFAULT 0,
    max_quantity_per_order INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_discount_positive CHECK (discount_percent > 0 AND discount_amount >= 0),
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0 AND sold_quantity >= 0),
    UNIQUE (flash_sale_id, product_id)
);

-- Thêm khóa ngoại cho flash_sale_item_id sau khi đã tạo bảng flash_sale_items
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_item_flash_sale_item
    FOREIGN KEY (flash_sale_item_id)
    REFERENCES flash_sale_items(id)
    ON DELETE SET NULL;

-- Tạo các index
CREATE INDEX IF NOT EXISTS idx_books_genre ON books(genre);
CREATE INDEX IF NOT EXISTS idx_books_publication_date ON books(publication_date);
CREATE INDEX IF NOT EXISTS idx_product_categories_product ON product_categories(product_id);
CREATE INDEX IF NOT EXISTS idx_product_categories_category ON product_categories(category_id);
CREATE INDEX IF NOT EXISTS idx_categories_type ON categories(type);
CREATE INDEX IF NOT EXISTS idx_flash_sale_items_product ON flash_sale_items(product_id);
CREATE INDEX IF NOT EXISTS idx_flash_sale_items_flash_sale ON flash_sale_items(flash_sale_id);
CREATE INDEX IF NOT EXISTS idx_flash_sales_status ON flash_sales(status);
CREATE INDEX IF NOT EXISTS idx_flash_sales_time ON flash_sales(start_time, end_time);

-- Chèn dữ liệu mẫu
INSERT INTO roles (name) VALUES
    ('ROLE_USER'),
    ('ROLE_MODERATOR'),
    ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;