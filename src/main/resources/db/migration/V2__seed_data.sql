-- ==========================================
-- V2: 测试种子数据
-- 登录密码说明：password_hash 存的是 MD5(明文)，明文见 README.md
-- ==========================================

-- 1. 门店（北京 5 / 上海 2 / 广州 2 / 深圳 1）
INSERT INTO stores (store_id, store_name, city, address, created_at) VALUES
    (1,  '7-ELEVEn Chaoyang Park',       'Beijing',   '1 Chaoyang Park Road, Chaoyang District',      '2026-04-01 08:00:00'),
    (2,  '7-ELEVEn Guomao CBD',          'Beijing',   '1 Jianguomenwai Avenue, Chaoyang District',    '2026-04-01 08:00:00'),
    (3,  '7-ELEVEn Zhongguancun',        'Beijing',   '15 Zhongguancun Street, Haidian District',     '2026-04-01 08:00:00'),
    (4,  '7-ELEVEn Wangfujing',          'Beijing',   '88 Wangfujing Street, Dongcheng District',     '2026-04-01 08:00:00'),
    (5,  '7-ELEVEn Sanlitun',            'Beijing',   '19 Sanlitun Road, Chaoyang District',          '2026-04-01 08:00:00'),
    (6,  '7-ELEVEn Lujiazui',            'Shanghai',  'Century Avenue, Pudong New Area',              '2026-04-01 08:00:00'),
    (7,  '7-ELEVEn Xintiandi',           'Shanghai',  'Madang Road, Huangpu District',                '2026-04-01 08:00:00'),
    (8,  '7-ELEVEn Tianhe Sports Center','Guangzhou', 'Tianhe Road, Tianhe District',                 '2026-04-01 08:00:00'),
    (9,  '7-ELEVEn Zhujiang New Town',   'Guangzhou', 'Huaxia Road, Tianhe District',                 '2026-04-01 08:00:00'),
    (10, '7-ELEVEn Futian CBD',          'Shenzhen',  'Fuhua Road, Futian District',                  '2026-04-01 08:00:00');

-- 2. 用户（1 ADMIN + 10 EMPLOYEE + 5 CONSUMER，密码入库即为 MD5）
INSERT INTO users (user_id, username, password_hash, role, status, store_id, balance, phone_number, created_at) VALUES
    (1,  'admin_super',  MD5('hash_admin_123'), 'ADMIN',    'NORMAL', NULL, 0.00,   '13800000000', '2026-04-01 09:00:00'),
    (2,  'emp_bj_01',    MD5('hash_emp_123'),   'EMPLOYEE', 'NORMAL', 1,    0.00,   '13800000001', '2026-04-02 08:00:00'),
    (3,  'emp_bj_02',    MD5('hash_emp_123'),   'EMPLOYEE', 'NORMAL', 2,    0.00,   '13800000002', '2026-04-02 08:00:00'),
    (4,  'emp_bj_03',    MD5('hash_emp_123'),   'EMPLOYEE', 'NORMAL', 3,    0.00,   '13800000003', '2026-04-02 08:00:00'),
    (5,  'emp_bj_04',    MD5('hash_emp_123'),   'EMPLOYEE', 'NORMAL', 4,    0.00,   '13800000004', '2026-04-02 08:00:00'),
    (6,  'emp_bj_05',    MD5('hash_emp_123'),   'EMPLOYEE', 'NORMAL', 5,    0.00,   '13800000005', '2026-04-02 08:00:00'),
    (7,  'emp_sh_01',    MD5('hash_emp_123'),   'EMPLOYEE', 'NORMAL', 6,    0.00,   '13800000006', '2026-04-02 08:00:00'),
    (8,  'emp_sh_02',    MD5('hash_emp_123'),   'EMPLOYEE', 'NORMAL', 7,    0.00,   '13800000007', '2026-04-02 08:00:00'),
    (9,  'emp_gz_01',    MD5('hash_emp_123'),   'EMPLOYEE', 'NORMAL', 8,    0.00,   '13800000008', '2026-04-02 08:00:00'),
    (10, 'emp_gz_02',    MD5('hash_emp_123'),   'EMPLOYEE', 'NORMAL', 9,    0.00,   '13800000009', '2026-04-02 08:00:00'),
    (11, 'emp_sz_01',    MD5('hash_emp_123'),   'EMPLOYEE', 'NORMAL', 10,   0.00,   '13800000010', '2026-04-02 08:00:00'),
    (12, 'user_alice',   MD5('hash_usr_123'),   'CONSUMER', 'NORMAL', NULL, 500.00, '13900000001', '2026-04-10 10:00:00'),
    (13, 'user_bob',     MD5('hash_usr_123'),   'CONSUMER', 'NORMAL', NULL, 20.50,  '13900000002', '2026-04-11 11:00:00'),
    (14, 'user_charlie', MD5('hash_usr_123'),   'CONSUMER', 'NORMAL', NULL, 300.00, '13900000003', '2026-04-12 12:00:00'),
    (15, 'user_diana',   MD5('hash_usr_123'),   'CONSUMER', 'NORMAL', NULL, 150.00, '13900000004', '2026-04-12 14:00:00'),
    (16, 'user_eve',     MD5('hash_usr_123'),   'CONSUMER', 'BANNED', NULL, 0.00,   '13900000005', '2026-04-13 09:00:00');

-- 3. 标准商品
INSERT INTO standard_products (barcode, product_name, normal_price, discount_rates) VALUES
    ('6901234560001', 'Teriyaki Chicken Bento',     15.80, '[1.0, 1.0, 1.0, 0.9, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2]'),
    ('6901234560002', 'Tuna Mayonnaise Onigiri',     4.50, '[1.0, 1.0, 0.9, 0.8, 0.8, 0.7, 0.6, 0.5, 0.5, 0.4, 0.3, 0.1]'),
    ('6901234560003', 'Meiji Fresh Milk 200ml',      6.00, '[1.0, 1.0, 1.0, 1.0, 0.9, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3]'),
    ('6901234560004', 'Ham & Egg Sandwich',          8.50, '[1.0, 0.9, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1]'),
    ('6901234560005', 'Original Hot Dog',            5.00, '[1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.8, 0.8, 0.6, 0.6, 0.4, 0.2]'),
    ('6901234560006', 'Spicy Chicken Wrap',         12.00, '[1.0, 1.0, 0.9, 0.9, 0.8, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2]'),
    ('6901234560007', 'Beef & Potato Stew Bento',   18.50, '[1.0, 1.0, 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1]'),
    ('6901234560008', 'Salmon Sushi Roll',          14.00, '[0.9, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1, 0.1]'),
    ('6901234560009', 'Matcha Swiss Roll',           9.90, '[1.0, 1.0, 1.0, 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2]'),
    ('6901234560010', 'Chocolate Chip Cookie',       6.50, '[1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.9, 0.8, 0.7, 0.5, 0.3]'),
    ('6901234560011', 'Suntory Oolong Tea 500ml',    5.50, '[1.0, 1.0, 1.0, 1.0, 1.0, 0.9, 0.9, 0.8, 0.8, 0.7, 0.6, 0.5]'),
    ('6901234560012', 'Iced Americano',             10.00, '[1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1, 0.1]'),
    ('6901234560013', 'Hot Latte',                  12.00, '[0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1, 0.1, 0.1]'),
    ('6901234560014', 'Pork Katsu Sandwich',        11.50, '[1.0, 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1]'),
    ('6901234560015', 'Strawberry Yogurt',           7.80, '[1.0, 1.0, 1.0, 0.9, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2]'),
    ('6901234560016', 'Mixed Fruit Salad',          13.50, '[0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1, 0.1, 0.1]'),
    ('6901234560017', 'Classic Cheesecake',         16.00, '[1.0, 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.2, 0.1]'),
    ('6901234560018', 'Grilled Eel Rice Bowl',      25.00, '[1.0, 1.0, 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1]'),
    ('6901234560019', 'Crab Stick Salad',           10.50, '[1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.1, 0.1]'),
    ('6901234560020', 'Roasted Sweet Potato',        6.00, '[1.0, 1.0, 1.0, 1.0, 0.8, 0.8, 0.6, 0.6, 0.4, 0.4, 0.2, 0.1]');

-- 4. 临期库存
INSERT INTO expiring_products (product_id, barcode, store_id, expiration_time, remaining_stock, status, created_by, created_at) VALUES
    (1,  '6901234560001', 1,  '2026-04-14 20:00:00', 3, 'AVAILABLE', 2,  '2026-04-14 08:00:00'),
    (2,  '6901234560002', 1,  '2026-04-14 16:00:00', 5, 'AVAILABLE', 2,  '2026-04-14 08:30:00'),
    (3,  '6901234560007', 2,  '2026-04-14 18:00:00', 2, 'AVAILABLE', 3,  '2026-04-14 09:00:00'),
    (4,  '6901234560004', 3,  '2026-04-14 12:00:00', 0, 'SOLD_OUT',  4,  '2026-04-13 22:00:00'),
    (5,  '6901234560018', 4,  '2026-04-13 23:59:00', 1, 'DISCARDED', 5,  '2026-04-13 10:00:00'),
    (6,  '6901234560009', 5,  '2026-04-14 22:00:00', 4, 'AVAILABLE', 6,  '2026-04-14 10:00:00'),
    (7,  '6901234560016', 6,  '2026-04-14 19:30:00', 2, 'AVAILABLE', 7,  '2026-04-14 09:15:00'),
    (8,  '6901234560012', 7,  '2026-04-14 15:00:00', 3, 'AVAILABLE', 8,  '2026-04-14 07:45:00'),
    (9,  '6901234560017', 8,  '2026-04-14 21:00:00', 1, 'AVAILABLE', 9,  '2026-04-14 11:00:00'),
    (10, '6901234560020', 10, '2026-04-14 23:00:00', 6, 'AVAILABLE', 11, '2026-04-14 12:30:00');

-- 5. 购物车
INSERT INTO shopping_cart (cart_item_id, user_id, product_id, quantity, added_at) VALUES
    (1, 12, 1, 1, '2026-04-14 13:00:00'),
    (2, 12, 2, 2, '2026-04-14 13:05:00'),
    (3, 14, 9, 1, '2026-04-14 14:00:00');

-- 6. 订单（覆盖全部 5 种状态）
INSERT INTO orders (order_id, user_id, store_id, total_amount, pickup_code, status, created_at, completed_at) VALUES
    (1, 13, 1,  12.00, 'PICKUP-711-A1B2C3', 'COMPLETED',       '2026-04-13 18:00:00', '2026-04-13 18:45:00'),
    (2, 14, 2,   7.50, 'PICKUP-711-X9Y8Z7', 'AWAITING_PICKUP', '2026-04-14 13:30:00', NULL),
    (3, 12, 6,  10.50, 'PICKUP-711-M4N5P6', 'PENDING',         '2026-04-14 14:10:00', NULL),
    (4, 13, 3,   4.00, 'PICKUP-711-Q1W2E3', 'CANCELLED',       '2026-04-14 09:00:00', NULL),
    (5, 15, 10,  5.00, 'PICKUP-711-P9O8I7', 'PAID',            '2026-04-14 14:40:00', NULL);

-- 7. 订单明细
INSERT INTO order_items (item_id, order_id, product_id, quantity, actual_price) VALUES
    (1, 1, 1,  1, 12.00),
    (2, 2, 3,  1,  4.20),
    (3, 2, 4,  1,  3.30),
    (4, 3, 7,  1, 10.50),
    (5, 4, 2,  1,  4.00),
    (6, 5, 10, 1,  5.00);
