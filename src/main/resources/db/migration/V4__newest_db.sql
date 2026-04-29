DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS shopping_cart;
DROP TABLE IF EXISTS expiring_products;
DROP TABLE IF EXISTS standard_products;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS stores;

-- ==========================================
-- 1. 基础信息模块 (门店与用户)
-- ==========================================

-- 1.1 门店表 (Stores)
CREATE TABLE stores (
                        store_id INT AUTO_INCREMENT PRIMARY KEY,
                        store_name VARCHAR(100) NOT NULL COMMENT '门店名称',
                        city VARCHAR(50) NOT NULL COMMENT '所在城市',
                        address VARCHAR(255) NOT NULL COMMENT '详细地址',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='7-11门店表';

-- 1.2 用户表 (Users)
CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
                       password_hash VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                       role ENUM('CONSUMER', 'EMPLOYEE', 'ADMIN') NOT NULL DEFAULT 'CONSUMER' COMMENT '用户角色',
                       status ENUM('NORMAL', 'BANNED') NOT NULL DEFAULT 'NORMAL' COMMENT '用户状态：正常、封禁',
                       store_id INT DEFAULT NULL COMMENT '员工关联门店ID',
                       balance DECIMAL(10, 2) DEFAULT 0.00 COMMENT '账户余额（真实货币）',
                       phone_number VARCHAR(20) COMMENT '联系电话',
                       user_address VARCHAR(50) COMMENT '用户地址',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
                       FOREIGN KEY (store_id) REFERENCES stores(store_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ==========================================
-- 2. 商品字典与降价逻辑模块
-- ==========================================

-- 2.1 系统标准商品表 (Standard Products)
CREATE TABLE standard_products (
                                   barcode VARCHAR(50) PRIMARY KEY COMMENT '商品条码',
                                   product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
                                   normal_price DECIMAL(10, 2) NOT NULL COMMENT '正常售价',
                                   discount_rates JSON NOT NULL DEFAULT (JSON_ARRAY(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0))
                                       COMMENT '临期前12小时逐小时折扣率数组，默认不打折'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标准商品字典表';

-- ==========================================
-- 3. 临期库存模块 (SKU/实例)
-- ==========================================

-- 3.1 临期商品上架表 (Expiring Products)
CREATE TABLE expiring_products (
                                   product_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '实际上架的商品ID',
                                   barcode VARCHAR(50) NOT NULL COMMENT '关联标准商品条码',
                                   store_id INT NOT NULL COMMENT '所属门店',
                                   expiration_time DATETIME NOT NULL COMMENT '具体过期时间',
                                   remaining_stock INT NOT NULL DEFAULT 1 COMMENT '剩余库存',
                                   status ENUM('AVAILABLE', 'SOLD_OUT', 'DISCARDED') NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：在售、售罄、报损',
                                   created_by INT COMMENT '录入员工ID',
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
                                   FOREIGN KEY (barcode) REFERENCES standard_products(barcode) ON UPDATE CASCADE,
                                   FOREIGN KEY (store_id) REFERENCES stores(store_id),
                                   FOREIGN KEY (created_by) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临期商品库存表';

-- ==========================================
-- 4. 交易与核销模块
-- ==========================================

-- 4.1 购物车表 (Shopping Cart)
CREATE TABLE shopping_cart (
                               cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT NOT NULL COMMENT '消费者ID',
                               product_id INT NOT NULL COMMENT '临期商品ID',
                               quantity INT NOT NULL DEFAULT 1,
                               added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
                               FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                               FOREIGN KEY (product_id) REFERENCES expiring_products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户购物车表';

-- 4.2 订单表 (Orders)
CREATE TABLE orders (
                        order_id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL COMMENT '购买者ID',
                        store_id INT NOT NULL COMMENT '提货门店ID',
                        total_amount DECIMAL(10, 2) NOT NULL COMMENT '实际支付总金额',
                        pickup_code VARCHAR(50) UNIQUE NOT NULL COMMENT '唯一核销码',
                        status ENUM('PENDING', 'CANCELLED', 'PAID', 'AWAITING_PICKUP', 'COMPLETED')
           NOT NULL DEFAULT 'PENDING'
           COMMENT '状态：待支付、已取消、已支付、待提货、已完成',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
                        completed_at DATETIME NULL COMMENT '核销完成时间',
                        FOREIGN KEY (user_id) REFERENCES users(user_id),
                        FOREIGN KEY (store_id) REFERENCES stores(store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自提订单表';

-- 4.3 订单明细表 (Order Items)
CREATE TABLE order_items (
                             item_id INT AUTO_INCREMENT PRIMARY KEY,
                             order_id INT NOT NULL COMMENT '关联订单',
                             product_id INT NOT NULL COMMENT '关联临期商品',
                             quantity INT NOT NULL DEFAULT 1,
                             actual_price DECIMAL(10, 2) NOT NULL COMMENT '购买时的折后单价',
                             FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
                             FOREIGN KEY (product_id) REFERENCES expiring_products(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细表';


-- 1. Insert Stores (10 stores total, 5 in Beijing)
INSERT INTO stores (store_id, store_name, city, address, created_at) VALUES
                                                                         (1, '7-ELEVEn Chaoyang Park', 'Beijing', '1 Chaoyang Park Road, Chaoyang District', '2026-04-01 08:00:00'),
                                                                         (2, '7-ELEVEn Guomao CBD', 'Beijing', '1 Jianguomenwai Avenue, Chaoyang District', '2026-04-01 08:00:00'),
                                                                         (3, '7-ELEVEn Zhongguancun', 'Beijing', '15 Zhongguancun Street, Haidian District', '2026-04-01 08:00:00'),
                                                                         (4, '7-ELEVEn Wangfujing', 'Beijing', '88 Wangfujing Street, Dongcheng District', '2026-04-01 08:00:00'),
                                                                         (5, '7-ELEVEn Sanlitun', 'Beijing', '19 Sanlitun Road, Chaoyang District', '2026-04-01 08:00:00'),
                                                                         (6, '7-ELEVEn Lujiazui', 'Shanghai', 'Century Avenue, Pudong New Area', '2026-04-01 08:00:00'),
                                                                         (7, '7-ELEVEn Xintiandi', 'Shanghai', 'Madang Road, Huangpu District', '2026-04-01 08:00:00'),
                                                                         (8, '7-ELEVEn Tianhe Sports Center', 'Guangzhou', 'Tianhe Road, Tianhe District', '2026-04-01 08:00:00'),
                                                                         (9, '7-ELEVEn Zhujiang New Town', 'Guangzhou', 'Huaxia Road, Tianhe District', '2026-04-01 08:00:00'),
                                                                         (10, '7-ELEVEn Futian CBD', 'Shenzhen', 'Fuhua Road, Futian District', '2026-04-01 08:00:00');

-- 2. Insert Users (1 Admin, 10 Employees mapped to stores, 5 Consumers)
INSERT INTO users (user_id, username, password_hash, role, status, store_id, balance, phone_number, user_address, created_at) VALUES
                                                                                                                                 (1, 'admin_super', 'c4ca4238a0b923820dcc509a6f75849b', 'ADMIN', 'NORMAL', NULL, 0.00, '13800000000', NULL, '2026-04-01 09:00:00'),
                                                                                                                                 (2, 'emp_bj_01', 'c4ca4238a0b923820dcc509a6f75849b', 'EMPLOYEE', 'NORMAL', 1, 0.00, '13800000001', NULL, '2026-04-02 08:00:00'),
                                                                                                                                 (3, 'emp_bj_02', 'c4ca4238a0b923820dcc509a6f75849b', 'EMPLOYEE', 'NORMAL', 2, 0.00, '13800000002', NULL, '2026-04-02 08:00:00'),
                                                                                                                                 (4, 'emp_bj_03', 'c4ca4238a0b923820dcc509a6f75849b', 'EMPLOYEE', 'NORMAL', 3, 0.00, '13800000003', NULL, '2026-04-02 08:00:00'),
                                                                                                                                 (5, 'emp_bj_04', 'c4ca4238a0b923820dcc509a6f75849b', 'EMPLOYEE', 'NORMAL', 4, 0.00, '13800000004', NULL, '2026-04-02 08:00:00'),
                                                                                                                                 (6, 'emp_bj_05', 'c4ca4238a0b923820dcc509a6f75849b', 'EMPLOYEE', 'NORMAL', 5, 0.00, '13800000005', NULL, '2026-04-02 08:00:00'),
                                                                                                                                 (7, 'emp_sh_01', 'c4ca4238a0b923820dcc509a6f75849b', 'EMPLOYEE', 'NORMAL', 6, 0.00, '13800000006', NULL, '2026-04-02 08:00:00'),
                                                                                                                                 (8, 'emp_sh_02', 'c4ca4238a0b923820dcc509a6f75849b', 'EMPLOYEE', 'NORMAL', 7, 0.00, '13800000007', NULL, '2026-04-02 08:00:00'),
                                                                                                                                 (9, 'emp_gz_01', 'c4ca4238a0b923820dcc509a6f75849b', 'EMPLOYEE', 'NORMAL', 8, 0.00, '13800000008', NULL, '2026-04-02 08:00:00'),
                                                                                                                                 (10, 'emp_gz_02', 'c4ca4238a0b923820dcc509a6f75849b', 'EMPLOYEE', 'NORMAL', 9, 0.00, '13800000009', NULL, '2026-04-02 08:00:00'),
                                                                                                                                 (11, 'emp_sz_01', 'c4ca4238a0b923820dcc509a6f75849b', 'EMPLOYEE', 'NORMAL', 10, 0.00, '13800000010', NULL, '2026-04-02 08:00:00'),
                                                                                                                                 (12, 'user_alice', 'c4ca4238a0b923820dcc509a6f75849b', 'CONSUMER', 'NORMAL', NULL, 500.00, '13900000001', 'Beijing', '2026-04-10 10:00:00'),
                                                                                                                                 (13, 'user_bob', 'c4ca4238a0b923820dcc509a6f75849b', 'CONSUMER', 'NORMAL', NULL, 20.50, '13900000002', 'Shanghai', '2026-04-11 11:00:00'),
                                                                                                                                 (14, 'user_charlie', 'c4ca4238a0b923820dcc509a6f75849b', 'CONSUMER', 'NORMAL', NULL, 300.00, '13900000003', 'Guangzhou', '2026-04-12 12:00:00'),
                                                                                                                                 (15, 'user_diana', 'c4ca4238a0b923820dcc509a6f75849b', 'CONSUMER', 'NORMAL', NULL, 150.00, '13900000004', 'Shenzhen', '2026-04-12 14:00:00'),
                                                                                                                                 (16, 'user_eve', 'c4ca4238a0b923820dcc509a6f75849b', 'CONSUMER', 'BANNED', NULL, 0.00, '13900000005', 'Beijing', '2026-04-13 09:00:00');

-- 3. Insert Standard Products (20 English products with varied discount arrays)
INSERT INTO standard_products (barcode, product_name, normal_price, discount_rates) VALUES
                                                                                        ('6901234560001', 'Teriyaki Chicken Bento', 15.80, '[1.0, 1.0, 1.0, 0.9, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2]'),
                                                                                        ('6901234560002', 'Tuna Mayonnaise Onigiri', 4.50, '[1.0, 1.0, 0.9, 0.8, 0.8, 0.7, 0.6, 0.5, 0.5, 0.4, 0.3, 0.1]'),
                                                                                        ('6901234560003', 'Meiji Fresh Milk 200ml', 6.00, '[1.0, 1.0, 1.0, 1.0, 0.9, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3]'),
                                                                                        ('6901234560004', 'Ham & Egg Sandwich', 8.50, '[1.0, 0.9, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1]'),
                                                                                        ('6901234560005', 'Original Hot Dog', 5.00, '[1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.8, 0.8, 0.6, 0.6, 0.4, 0.2]'),
                                                                                        ('6901234560006', 'Spicy Chicken Wrap', 12.00, '[1.0, 1.0, 0.9, 0.9, 0.8, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2]'),
                                                                                        ('6901234560007', 'Beef & Potato Stew Bento', 18.50, '[1.0, 1.0, 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1]'),
                                                                                        ('6901234560008', 'Salmon Sushi Roll', 14.00, '[0.9, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1, 0.1]'),
                                                                                        ('6901234560009', 'Matcha Swiss Roll', 9.90, '[1.0, 1.0, 1.0, 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2]'),
                                                                                        ('6901234560010', 'Chocolate Chip Cookie', 6.50, '[1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9, 0.8, 0.7, 0.5, 0.3]'),
                                                                                        ('6901234560011', 'Suntory Oolong Tea 500ml', 5.50, '[1.0, 1.0, 1.0, 1.0, 1.0, 0.9, 0.9, 0.8, 0.8, 0.7, 0.6, 0.5]'),
                                                                                        ('6901234560012', 'Iced Americano', 10.00, '[1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1, 0.1]'),
                                                                                        ('6901234560013', 'Hot Latte', 12.00, '[0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1, 0.1, 0.1]'),
                                                                                        ('6901234560014', 'Pork Katsu Sandwich', 11.50, '[1.0, 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1]'),
                                                                                        ('6901234560015', 'Strawberry Yogurt', 7.80, '[1.0, 1.0, 1.0, 0.9, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2]'),
                                                                                        ('6901234560016', 'Mixed Fruit Salad', 13.50, '[0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1, 0.1, 0.1]'),
                                                                                        ('6901234560017', 'Classic Cheesecake', 16.00, '[1.0, 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.2, 0.1]'),
                                                                                        ('6901234560018', 'Grilled Eel Rice Bowl', 25.00, '[1.0, 1.0, 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1]'),
                                                                                        ('6901234560019', 'Crab Stick Salad', 10.50, '[1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1, 0.1]'),
                                                                                        ('6901234560020', 'Roasted Sweet Potato', 6.00, '[1.0, 1.0, 1.0, 1.0, 0.8, 0.8, 0.6, 0.6, 0.4, 0.4, 0.2, 0.1]');

-- 4. Insert Expiring Products (Distributed across different stores)
INSERT INTO expiring_products (product_id, barcode, store_id, expiration_time, remaining_stock, status, created_by, created_at) VALUES
                                                                                                                                    (1, '6901234560001', 1, '2026-04-14 20:00:00', 3, 'AVAILABLE', 2, '2026-04-14 08:00:00'), -- Beijing Store 1
                                                                                                                                    (2, '6901234560002', 1, '2026-04-14 16:00:00', 5, 'AVAILABLE', 2, '2026-04-14 08:30:00'), -- Beijing Store 1
                                                                                                                                    (3, '6901234560007', 2, '2026-04-14 18:00:00', 2, 'AVAILABLE', 3, '2026-04-14 09:00:00'), -- Beijing Store 2
                                                                                                                                    (4, '6901234560004', 3, '2026-04-14 12:00:00', 0, 'SOLD_OUT', 4, '2026-04-13 22:00:00'),  -- Beijing Store 3
                                                                                                                                    (5, '6901234560018', 4, '2026-04-13 23:59:00', 1, 'DISCARDED', 5, '2026-04-13 10:00:00'),  -- Beijing Store 4
                                                                                                                                    (6, '6901234560009', 5, '2026-04-14 22:00:00', 4, 'AVAILABLE', 6, '2026-04-14 10:00:00'), -- Beijing Store 5
                                                                                                                                    (7, '6901234560016', 6, '2026-04-14 19:30:00', 2, 'AVAILABLE', 7, '2026-04-14 09:15:00'), -- Shanghai Store 6
                                                                                                                                    (8, '6901234560012', 7, '2026-04-14 15:00:00', 3, 'AVAILABLE', 8, '2026-04-14 07:45:00'), -- Shanghai Store 7
                                                                                                                                    (9, '6901234560017', 8, '2026-04-14 21:00:00', 1, 'AVAILABLE', 9, '2026-04-14 11:00:00'), -- Guangzhou Store 8
                                                                                                                                    (10, '6901234560020', 10, '2026-04-14 23:00:00', 6, 'AVAILABLE', 11, '2026-04-14 12:30:00');-- Shenzhen Store 10

-- 5. Insert Shopping Cart Data
INSERT INTO shopping_cart (cart_item_id, user_id, product_id, quantity, added_at) VALUES
                                                                                      (1, 12, 1, 1, '2026-04-14 13:00:00'), -- Alice (12) added product 1 (Bento)
                                                                                      (2, 12, 2, 2, '2026-04-14 13:05:00'), -- Alice (12) added product 2 (Onigiri)
                                                                                      (3, 14, 9, 1, '2026-04-14 14:00:00'); -- Charlie (14) added product 9 (Cheesecake)

-- 6. Insert Orders (Covering all states in English flow)
INSERT INTO orders (order_id, user_id, store_id, total_amount, pickup_code, status, created_at, completed_at) VALUES
                                                                                                                  (1, 13, 1, 12.00, 'PICKUP-711-A1B2C3', 'COMPLETED', '2026-04-13 18:00:00', '2026-04-13 18:45:00'),       -- Bob: completed
                                                                                                                  (2, 14, 2, 7.50, 'PICKUP-711-X9Y8Z7', 'AWAITING_PICKUP', '2026-04-14 13:30:00', NULL),                 -- Charlie: waiting for pickup
                                                                                                                  (3, 12, 6, 10.50, 'PICKUP-711-M4N5P6', 'PENDING', '2026-04-14 14:10:00', NULL),                      -- Alice: pending payment
                                                                                                                  (4, 13, 3, 4.00, 'PICKUP-711-Q1W2E3', 'CANCELLED', '2026-04-14 09:00:00', NULL),                     -- Bob: cancelled
                                                                                                                  (5, 15, 10, 5.00, 'PICKUP-711-P9O8I7', 'PAID', '2026-04-14 14:40:00', NULL);                         -- Diana: paid (not yet awaiting pickup)

-- 7. Insert Order Items
INSERT INTO order_items (item_id, order_id, product_id, quantity, actual_price) VALUES
                                                                                    (1, 1, 1, 1, 12.00), -- Order 1: Teriyaki Chicken Bento
                                                                                    (2, 2, 3, 1, 4.20),  -- Order 2: Beef & Potato Stew Bento
                                                                                    (3, 2, 4, 1, 3.30),  -- Order 2: Ham & Egg Sandwich
                                                                                    (4, 3, 7, 1, 10.50), -- Order 3: Mixed Fruit Salad
                                                                                    (5, 4, 2, 1, 4.00),  -- Order 4: Tuna Mayonnaise Onigiri
                                                                                    (6, 5, 10, 1, 5.00); -- Order 5: Roasted Sweet Potato


