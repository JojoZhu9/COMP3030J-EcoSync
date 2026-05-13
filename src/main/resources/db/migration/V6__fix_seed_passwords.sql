-- V6: 统一种子用户密码为 MD5('1')，与 README 测试账号密码一致
-- 修复服务器上种子用户无法登录的问题（V2 的 MD5('hash_xxx_123') 与期望密码 '1' 不匹配）

UPDATE users SET password_hash = MD5('1')
WHERE user_id BETWEEN 1 AND 16;
