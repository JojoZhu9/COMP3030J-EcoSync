CREATE TABLE IF NOT EXISTS stores (
    store_id INT AUTO_INCREMENT PRIMARY KEY,
    store_name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CONSUMER', 'EMPLOYEE', 'ADMIN') NOT NULL DEFAULT 'CONSUMER',
    store_id INT DEFAULT NULL,
    points INT DEFAULT 0,
    phone_number VARCHAR(20),
    default_address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_store
        FOREIGN KEY (store_id) REFERENCES stores(store_id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS standard_products (
    barcode VARCHAR(50) PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    normal_price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS pricing_rules (
    rule_id INT AUTO_INCREMENT PRIMARY KEY,
    barcode VARCHAR(50) NOT NULL,
    hours_to_expiration INT NOT NULL,
    discount_rate DECIMAL(4, 2) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pricing_rules_product
        FOREIGN KEY (barcode) REFERENCES standard_products(barcode)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS expiring_products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    barcode VARCHAR(50) NOT NULL,
    store_id INT NOT NULL,
    expiration_time DATETIME NOT NULL,
    remaining_stock INT NOT NULL DEFAULT 1,
    status ENUM('AVAILABLE', 'SOLD_OUT', 'DISCARDED') NOT NULL DEFAULT 'AVAILABLE',
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expiring_products_barcode
        FOREIGN KEY (barcode) REFERENCES standard_products(barcode)
        ON UPDATE CASCADE,
    CONSTRAINT fk_expiring_products_store
        FOREIGN KEY (store_id) REFERENCES stores(store_id),
    CONSTRAINT fk_expiring_products_creator
        FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS shopping_cart (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shopping_cart_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_shopping_cart_product
        FOREIGN KEY (product_id) REFERENCES expiring_products(product_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    store_id INT NOT NULL,
    total_points_spent INT NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    status ENUM('PENDING', 'DELIVERING', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at DATETIME NULL,
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_orders_store
        FOREIGN KEY (store_id) REFERENCES stores(store_id)
);

CREATE TABLE IF NOT EXISTS order_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    points_price INT NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES expiring_products(product_id)
);
