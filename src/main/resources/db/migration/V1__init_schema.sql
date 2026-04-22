-- ==========================================
-- V1: EcoSync 711ex 基础表结构
-- ==========================================

-- 1.1 门店表
CREATE TABLE IF NOT EXISTS stores (
    store_id   INT AUTO_INCREMENT PRIMARY KEY,
    store_name VARCHAR(100) NOT NULL COMMENT '门店名称',
    city       VARCHAR(50)  NOT NULL COMMENT '所在城市',
    address    VARCHAR(255) NOT NULL COMMENT '详细地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='7-11门店表';

-- 1.2 用户表
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT 'MD5 加密后的密码',
    role          ENUM('CONSUMER', 'EMPLOYEE', 'ADMIN') NOT NULL DEFAULT 'CONSUMER' COMMENT '用户角色',
    status        ENUM('NORMAL', 'BANNED')              NOT NULL DEFAULT 'NORMAL'   COMMENT '用户状态：正常、封禁',
    store_id      INT DEFAULT NULL COMMENT '员工关联门店ID',
    balance       DECIMAL(10, 2) DEFAULT 0.00 COMMENT '账户余额（真实货币）',
    phone_number  VARCHAR(20) COMMENT '联系电话',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_store
        FOREIGN KEY (store_id) REFERENCES stores(store_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2.1 系统标准商品表
CREATE TABLE IF NOT EXISTS standard_products (
    barcode        VARCHAR(50)  PRIMARY KEY COMMENT '商品条码',
    product_name   VARCHAR(100) NOT NULL COMMENT '商品名称',
    normal_price   DECIMAL(10, 2) NOT NULL COMMENT '正常售价',
    discount_rates JSON NOT NULL DEFAULT (JSON_ARRAY(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0))
        COMMENT '临期前12小时逐小时折扣率数组，默认不打折'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标准商品字典表';

-- 3.1 临期商品上架表
CREATE TABLE IF NOT EXISTS expiring_products (
    product_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '实际上架的商品ID',
    barcode         VARCHAR(50) NOT NULL COMMENT '关联标准商品条码',
    store_id        INT NOT NULL COMMENT '所属门店',
    expiration_time DATETIME NOT NULL COMMENT '具体过期时间',
    remaining_stock INT NOT NULL DEFAULT 1 COMMENT '剩余库存',
    status          ENUM('AVAILABLE', 'SOLD_OUT', 'DISCARDED') NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：在售、售罄、报损',
    created_by      INT COMMENT '录入员工ID',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expiring_products_barcode
        FOREIGN KEY (barcode) REFERENCES standard_products(barcode) ON UPDATE CASCADE,
    CONSTRAINT fk_expiring_products_store
        FOREIGN KEY (store_id) REFERENCES stores(store_id),
    CONSTRAINT fk_expiring_products_creator
        FOREIGN KEY (created_by) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临期商品库存表';

-- 4.1 购物车表
CREATE TABLE IF NOT EXISTS shopping_cart (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL COMMENT '消费者ID',
    product_id   INT NOT NULL COMMENT '临期商品ID',
    quantity     INT NOT NULL DEFAULT 1,
    added_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shopping_cart_user
        FOREIGN KEY (user_id)    REFERENCES users(user_id)              ON DELETE CASCADE,
    CONSTRAINT fk_shopping_cart_product
        FOREIGN KEY (product_id) REFERENCES expiring_products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户购物车表';

-- 4.2 订单表
CREATE TABLE IF NOT EXISTS orders (
    order_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL COMMENT '购买者ID',
    store_id     INT NOT NULL COMMENT '提货门店ID',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '实际支付总金额',
    pickup_code  VARCHAR(50) UNIQUE NOT NULL COMMENT '唯一核销码',
    status       ENUM('PENDING', 'CANCELLED', 'PAID', 'AWAITING_PICKUP', 'COMPLETED')
                 NOT NULL DEFAULT 'PENDING'
                 COMMENT '状态：待支付、已取消、已支付、待提货、已完成',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME NULL COMMENT '核销完成时间',
    CONSTRAINT fk_orders_user  FOREIGN KEY (user_id)  REFERENCES users(user_id),
    CONSTRAINT fk_orders_store FOREIGN KEY (store_id) REFERENCES stores(store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自提订单表';

-- 4.3 订单明细表
CREATE TABLE IF NOT EXISTS order_items (
    item_id      INT AUTO_INCREMENT PRIMARY KEY,
    order_id     INT NOT NULL COMMENT '关联订单',
    product_id   INT NOT NULL COMMENT '关联临期商品',
    quantity     INT NOT NULL DEFAULT 1,
    actual_price DECIMAL(10, 2) NOT NULL COMMENT '购买时的折后单价',
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)   REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES expiring_products(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细表';
