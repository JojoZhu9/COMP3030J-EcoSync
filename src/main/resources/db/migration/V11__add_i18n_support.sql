-- ==========================================
-- V11: 数据库国际化支持
-- 为 stores / standard_products 添加多语言字段并填充双语数据
-- ==========================================

-- 1. 为 stores 表添加英文字段（安全模式，已存在则跳过）
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stores' AND COLUMN_NAME = 'store_name_en');
SET @stmt = IF(@col_exists = 0, 'ALTER TABLE stores ADD COLUMN store_name_en VARCHAR(100) DEFAULT NULL COMMENT ''门店名称（英文）'' AFTER store_name', 'SELECT 1');
PREPARE st FROM @stmt; EXECUTE st; DEALLOCATE PREPARE st;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stores' AND COLUMN_NAME = 'city_en');
SET @stmt = IF(@col_exists = 0, 'ALTER TABLE stores ADD COLUMN city_en VARCHAR(50) DEFAULT NULL COMMENT ''所在城市（英文）'' AFTER city', 'SELECT 1');
PREPARE st FROM @stmt; EXECUTE st; DEALLOCATE PREPARE st;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stores' AND COLUMN_NAME = 'address_en');
SET @stmt = IF(@col_exists = 0, 'ALTER TABLE stores ADD COLUMN address_en VARCHAR(255) DEFAULT NULL COMMENT ''详细地址（英文）'' AFTER address', 'SELECT 1');
PREPARE st FROM @stmt; EXECUTE st; DEALLOCATE PREPARE st;

-- 2. 为 standard_products 表添加英文字段
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'standard_products' AND COLUMN_NAME = 'product_name_en');
SET @stmt = IF(@col_exists = 0, 'ALTER TABLE standard_products ADD COLUMN product_name_en VARCHAR(100) DEFAULT NULL COMMENT ''商品名称（英文）'' AFTER product_name', 'SELECT 1');
PREPARE st FROM @stmt; EXECUTE st; DEALLOCATE PREPARE st;

-- 3. 更新门店中文数据（idempotent：兼容已修复库和全新库）
UPDATE stores SET store_name = '7-ELEVEn 朝阳公园店', city = '北京', address = '朝阳区朝阳公园路1号' WHERE store_id = 1;
UPDATE stores SET store_name = '7-ELEVEn 国贸CBD店', city = '北京', address = '朝阳区建国门外大街1号' WHERE store_id = 2;
UPDATE stores SET store_name = '7-ELEVEn 中关村店', city = '北京', address = '海淀区中关村大街15号' WHERE store_id = 3;
UPDATE stores SET store_name = '7-ELEVEn 王府井店', city = '北京', address = '东城区王府井大街88号' WHERE store_id = 4;
UPDATE stores SET store_name = '7-ELEVEn 三里屯店', city = '北京', address = '朝阳区三里屯路19号' WHERE store_id = 5;
UPDATE stores SET store_name = '7-ELEVEn 陆家嘴店', city = '上海', address = '浦东新区世纪大道' WHERE store_id = 6;
UPDATE stores SET store_name = '7-ELEVEn 新天地店', city = '上海', address = '黄浦区马当路' WHERE store_id = 7;
UPDATE stores SET store_name = '7-ELEVEn 天河体育中心店', city = '广州', address = '天河区天河路' WHERE store_id = 8;
UPDATE stores SET store_name = '7-ELEVEn 珠江新城店', city = '广州', address = '天河区华夏路' WHERE store_id = 9;
UPDATE stores SET store_name = '7-ELEVEn 福田CBD店', city = '深圳', address = '福田区福华路' WHERE store_id = 10;

-- 4. 更新门店英文数据
UPDATE stores SET store_name_en = '7-ELEVEn Chaoyang Park', city_en = 'Beijing', address_en = '1 Chaoyang Park Road, Chaoyang District' WHERE store_id = 1;
UPDATE stores SET store_name_en = '7-ELEVEn Guomao CBD', city_en = 'Beijing', address_en = '1 Jianguomenwai Avenue, Chaoyang District' WHERE store_id = 2;
UPDATE stores SET store_name_en = '7-ELEVEn Zhongguancun', city_en = 'Beijing', address_en = '15 Zhongguancun Street, Haidian District' WHERE store_id = 3;
UPDATE stores SET store_name_en = '7-ELEVEn Wangfujing', city_en = 'Beijing', address_en = '88 Wangfujing Street, Dongcheng District' WHERE store_id = 4;
UPDATE stores SET store_name_en = '7-ELEVEn Sanlitun', city_en = 'Beijing', address_en = '19 Sanlitun Road, Chaoyang District' WHERE store_id = 5;
UPDATE stores SET store_name_en = '7-ELEVEn Lujiazui', city_en = 'Shanghai', address_en = 'Century Avenue, Pudong New Area' WHERE store_id = 6;
UPDATE stores SET store_name_en = '7-ELEVEn Xintiandi', city_en = 'Shanghai', address_en = 'Madang Road, Huangpu District' WHERE store_id = 7;
UPDATE stores SET store_name_en = '7-ELEVEn Tianhe Sports Center', city_en = 'Guangzhou', address_en = 'Tianhe Road, Tianhe District' WHERE store_id = 8;
UPDATE stores SET store_name_en = '7-ELEVEn Zhujiang New Town', city_en = 'Guangzhou', address_en = 'Huaxia Road, Tianhe District' WHERE store_id = 9;
UPDATE stores SET store_name_en = '7-ELEVEn Futian CBD', city_en = 'Shenzhen', address_en = 'Fuhua Road, Futian District' WHERE store_id = 10;

-- 5. 更新商品中文与英文数据（全部 60 条，idempotent）
UPDATE standard_products SET product_name = '照烧鸡肉便当', product_name_en = 'Teriyaki Chicken Bento' WHERE barcode = '6901234560001';
UPDATE standard_products SET product_name = '金枪鱼蛋黄酱饭团', product_name_en = 'Tuna Mayonnaise Onigiri' WHERE barcode = '6901234560002';
UPDATE standard_products SET product_name = '明治鲜牛奶 200ml', product_name_en = 'Meiji Fresh Milk 200ml' WHERE barcode = '6901234560003';
UPDATE standard_products SET product_name = '火腿鸡蛋三明治', product_name_en = 'Ham & Egg Sandwich' WHERE barcode = '6901234560004';
UPDATE standard_products SET product_name = '原味热狗', product_name_en = 'Original Hot Dog' WHERE barcode = '6901234560005';
UPDATE standard_products SET product_name = '香辣鸡肉卷', product_name_en = 'Spicy Chicken Wrap' WHERE barcode = '6901234560006';
UPDATE standard_products SET product_name = '土豆炖牛肉便当', product_name_en = 'Beef & Potato Stew Bento' WHERE barcode = '6901234560007';
UPDATE standard_products SET product_name = '三文鱼寿司卷', product_name_en = 'Salmon Sushi Roll' WHERE barcode = '6901234560008';
UPDATE standard_products SET product_name = '抹茶瑞士卷', product_name_en = 'Matcha Swiss Roll' WHERE barcode = '6901234560009';
UPDATE standard_products SET product_name = '巧克力曲奇', product_name_en = 'Chocolate Chip Cookie' WHERE barcode = '6901234560010';
UPDATE standard_products SET product_name = '三得利乌龙茶 500ml', product_name_en = 'Suntory Oolong Tea 500ml' WHERE barcode = '6901234560011';
UPDATE standard_products SET product_name = '冰美式咖啡', product_name_en = 'Iced Americano' WHERE barcode = '6901234560012';
UPDATE standard_products SET product_name = '热拿铁', product_name_en = 'Hot Latte' WHERE barcode = '6901234560013';
UPDATE standard_products SET product_name = '炸猪排三明治', product_name_en = 'Pork Katsu Sandwich' WHERE barcode = '6901234560014';
UPDATE standard_products SET product_name = '草莓酸奶', product_name_en = 'Strawberry Yogurt' WHERE barcode = '6901234560015';
UPDATE standard_products SET product_name = '混合水果沙拉', product_name_en = 'Mixed Fruit Salad' WHERE barcode = '6901234560016';
UPDATE standard_products SET product_name = '经典芝士蛋糕', product_name_en = 'Classic Cheesecake' WHERE barcode = '6901234560017';
UPDATE standard_products SET product_name = '蒲烧鳗鱼饭', product_name_en = 'Grilled Eel Rice Bowl' WHERE barcode = '6901234560018';
UPDATE standard_products SET product_name = '蟹棒沙拉', product_name_en = 'Crab Stick Salad' WHERE barcode = '6901234560019';
UPDATE standard_products SET product_name = '烤红薯', product_name_en = 'Roasted Sweet Potato' WHERE barcode = '6901234560020';
UPDATE standard_products SET product_name = '蔬菜春卷', product_name_en = 'Veggie Spring Roll' WHERE barcode = '6901234560021';
UPDATE standard_products SET product_name = '叉烧包', product_name_en = 'BBQ Pork Bun' WHERE barcode = '6901234560022';
UPDATE standard_products SET product_name = '芒果布丁', product_name_en = 'Mango Pudding' WHERE barcode = '6901234560023';
UPDATE standard_products SET product_name = '鸡蛋沙拉三明治', product_name_en = 'Egg Salad Sandwich' WHERE barcode = '6901234560024';
UPDATE standard_products SET product_name = '玉米热狗', product_name_en = 'Corn Dog' WHERE barcode = '6901234560025';
UPDATE standard_products SET product_name = '照烧三文鱼卷', product_name_en = 'Teriyaki Salmon Wrap' WHERE barcode = '6901234560026';
UPDATE standard_products SET product_name = '炸鸡排便当', product_name_en = 'Chicken Katsu Bento' WHERE barcode = '6901234560027';
UPDATE standard_products SET product_name = '鲜虾天妇罗卷', product_name_en = 'Shrimp Tempura Roll' WHERE barcode = '6901234560028';
UPDATE standard_products SET product_name = '红豆麻糬', product_name_en = 'Red Bean Mochi' WHERE barcode = '6901234560029';
UPDATE standard_products SET product_name = '燕麦葡萄干曲奇', product_name_en = 'Oatmeal Raisin Cookie' WHERE barcode = '6901234560030';
UPDATE standard_products SET product_name = '麒麟柠檬茶 500ml', product_name_en = 'Kirin Lemon Tea 500ml' WHERE barcode = '6901234560031';
UPDATE standard_products SET product_name = '冷萃咖啡', product_name_en = 'Cold Brew Coffee' WHERE barcode = '6901234560032';
UPDATE standard_products SET product_name = '焦糖玛奇朵', product_name_en = 'Caramel Macchiato' WHERE barcode = '6901234560033';
UPDATE standard_products SET product_name = '总会三明治', product_name_en = 'Club Sandwich' WHERE barcode = '6901234560034';
UPDATE standard_products SET product_name = '黄桃酸奶', product_name_en = 'Peach Yogurt' WHERE barcode = '6901234560035';
UPDATE standard_products SET product_name = '凯撒沙拉', product_name_en = 'Caesar Salad' WHERE barcode = '6901234560036';
UPDATE standard_products SET product_name = '提拉米苏切片', product_name_en = 'Tiramisu Slice' WHERE barcode = '6901234560037';
UPDATE standard_products SET product_name = '牛肉盖饭', product_name_en = 'Beef Rice Bowl' WHERE barcode = '6901234560038';
UPDATE standard_products SET product_name = '海藻沙拉', product_name_en = 'Seaweed Salad' WHERE barcode = '6901234560039';
UPDATE standard_products SET product_name = '蜂蜜黄油薯', product_name_en = 'Honey Butter Potato' WHERE barcode = '6901234560040';
UPDATE standard_products SET product_name = '泡菜炒饭', product_name_en = 'Kimchi Fried Rice' WHERE barcode = '6901234560041';
UPDATE standard_products SET product_name = '金枪鱼寿司', product_name_en = 'Tuna Sushi' WHERE barcode = '6901234560042';
UPDATE standard_products SET product_name = '豆浆 300ml', product_name_en = 'Soy Milk 300ml' WHERE barcode = '6901234560043';
UPDATE standard_products SET product_name = '培根生菜番茄三明治', product_name_en = 'BLT Sandwich' WHERE barcode = '6901234560044';
UPDATE standard_products SET product_name = '芝士热狗', product_name_en = 'Cheese Hot Dog' WHERE barcode = '6901234560045';
UPDATE standard_products SET product_name = '蔬菜卷', product_name_en = 'Veggie Wrap' WHERE barcode = '6901234560046';
UPDATE standard_products SET product_name = '五花肉便当', product_name_en = 'Pork Belly Bento' WHERE barcode = '6901234560047';
UPDATE standard_products SET product_name = '牛油果寿司卷', product_name_en = 'Avocado Sushi Roll' WHERE barcode = '6901234560048';
UPDATE standard_products SET product_name = '芝麻麻糬', product_name_en = 'Sesame Mochi' WHERE barcode = '6901234560049';
UPDATE standard_products SET product_name = '双层巧克力曲奇', product_name_en = 'Double Chocolate Cookie' WHERE barcode = '6901234560050';
UPDATE standard_products SET product_name = '抹茶拿铁', product_name_en = 'Green Tea Latte' WHERE barcode = '6901234560051';
UPDATE standard_products SET product_name = '浓缩咖啡', product_name_en = 'Espresso' WHERE barcode = '6901234560052';
UPDATE standard_products SET product_name = '香草拿铁', product_name_en = 'Vanilla Latte' WHERE barcode = '6901234560053';
UPDATE standard_products SET product_name = '火鸡三明治', product_name_en = 'Turkey Sandwich' WHERE barcode = '6901234560054';
UPDATE standard_products SET product_name = '蓝莓酸奶', product_name_en = 'Blueberry Yogurt' WHERE barcode = '6901234560055';
UPDATE standard_products SET product_name = '希腊沙拉', product_name_en = 'Greek Salad' WHERE barcode = '6901234560056';
UPDATE standard_products SET product_name = '蓝莓芝士蛋糕', product_name_en = 'Blueberry Cheesecake' WHERE barcode = '6901234560057';
UPDATE standard_products SET product_name = '咖喱鸡肉饭', product_name_en = 'Chicken Curry Bowl' WHERE barcode = '6901234560058';
UPDATE standard_products SET product_name = '豆腐沙拉', product_name_en = 'Tofu Salad' WHERE barcode = '6901234560059';
UPDATE standard_products SET product_name = '蒜香黄油薯', product_name_en = 'Garlic Butter Potato' WHERE barcode = '6901234560060';
