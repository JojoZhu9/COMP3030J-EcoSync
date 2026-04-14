# EcoSync 数据库迁移规范（Flyway）

本文档用于规范项目后续的数据库表结构变更流程，目标是：

- 保留线上数据
- 可回溯、可审计
- 降低多人协作冲突和上线风险

---

## 1. 当前方案说明

项目已接入 Flyway，配置在 `src/main/resources/application.yml`：

- `spring.flyway.enabled: true`
- `spring.flyway.locations: classpath:db/migration`
- `spring.flyway.baseline-on-migrate: true`
- `spring.flyway.baseline-version: 1`

迁移脚本目录：

- `src/main/resources/db/migration/`

基线脚本：

- `V1__init_schema.sql`

---

## 2. 迁移文件命名规则

文件名格式：

`V<版本号>__<英文描述>.sql`

示例：

- `V2__add_user_email.sql`
- `V3__add_index_for_orders_status.sql`
- `V4__add_order_total_update_field.sql`

要求：

- 版本号必须递增，且唯一
- 描述使用下划线分隔，语义清晰
- 禁止修改已执行过的历史版本文件

---

## 3. 标准变更流程（必须执行）

1. **新建迁移脚本**
   - 在 `db/migration` 目录新增一个更高版本的 SQL 文件

2. **仅写增量变更 SQL**
   - 推荐：`ALTER TABLE`、`CREATE INDEX`、`UPDATE`（必要的数据回填）
   - 避免：`DROP TABLE`、`TRUNCATE`、全量重建

3. **本地验证**
   - 启动应用，确认 Flyway 自动执行迁移
   - 检查 `flyway_schema_history` 有新增版本记录

4. **提交到 main**
   - 迁移脚本与对应代码一并提交

5. **部署后校验**
   - 检查生产库迁移记录与表结构

---

## 4. 常用检查 SQL

查看迁移历史：

```sql
SELECT installed_rank, version, description, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

查看表结构：

```sql
SHOW CREATE TABLE users;
SHOW CREATE TABLE orders;
SHOW CREATE TABLE expiring_products;
```

---

## 5. 示例：新增字段

目标：给 `users` 增加邮箱字段。

新建文件：`V2__add_user_email.sql`

```sql
ALTER TABLE users ADD COLUMN email VARCHAR(100) NULL;
CREATE INDEX idx_users_email ON users(email);
```

---

## 6. 上线安全规范

- 每次变更前先备份数据库
- 高风险变更使用“两阶段迁移”：
  1) 先加新列（可空）并回填
  2) 代码切换读取新列
  3) 下一版本再删旧列
- 生产环境禁止手工直接改表（除紧急故障，经审批）
- 不在同一迁移里混入过多无关变更

---

## 7. 失败处理建议

如果迁移失败：

1. 先修复 SQL 或数据问题
2. 不要直接删 `flyway_schema_history` 记录
3. 在确认影响范围后执行修复迁移（新版本脚本）
4. 极端情况由负责人评估后再做手工干预

---

## 8. 注意事项（针对本项目）

- 项目已移除 `docker-compose` 中旧的 `/docker-entrypoint-initdb.d/init.sql` 自动初始化挂载
- CI 已改为由 Flyway 在应用启动阶段完成迁移
- `init_711_db.sql` 可作为历史参考，不再作为后续改表入口

---

维护人：EcoSync 开发团队  
最后更新：2026-04-14

