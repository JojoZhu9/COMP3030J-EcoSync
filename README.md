# EcoSync · 7-ELEVEn 临期商品平台

一个基于 Spring Boot 3 + MyBatis + MySQL 8 的临期食品平台，覆盖门店管理、标准商品字典、临期库存上架、消费者下单与到店自提核销全流程。

---

## 技术栈

- **后端**：Spring Boot 3.2 / MyBatis / MySQL 8 / Flyway（数据库版本管理）/ JWT
- **接口文档**：Knife4j (OpenAPI 3)
- **前端**：见 `ecosync-frontend/`
- **容器化**：`docker-compose.yml`（MySQL + App + Frontend + Gateway Nginx）

---

## 快速启动

下面提供两种启动方式：**全 Docker 一键启动** 和 **本地开发启动**。  
建议先看完你要走的方式，再直接复制命令执行。

### 方式一：Docker Compose 一键启动（推荐）

适合想快速跑完整环境（前端 + 后端 + MySQL）的人。

#### 1) 进入项目根目录

```powershell
cd EcoSync
```

#### 2) 启动全部服务（首次会自动构建镜像）

```powershell
$env:DOCKER_BUILDKIT=1
docker compose up -d --build
```

> 首次启动时间较长（通常 3-10 分钟），取决于网络与镜像缓存情况。
> 启动后会有 4 个核心容器：`ecosync-mysql`、`ecosync-app`、`ecosync-frontend`、`ecosync-gateway`。
> MySQL 对外访问端口为 `3307`（由网关转发到 MySQL 容器 `3306`）。

#### 3) 查看服务状态（可选）

```powershell
docker ps --filter "name=ecosync"
docker compose logs -f
```

#### 4) 进入网站 / 文档

- 网站首页（前端入口）：<http://localhost>
- 接口文档（Knife4j）：<http://localhost/doc.html>
- OpenAPI JSON：<http://localhost/v3/api-docs>

> 说明：在 Docker 方案中，后端 `8080` 端口是容器内部端口，不直接暴露到宿主机；统一从 `http://localhost` 进入。

#### 5) 停止服务

```powershell
docker compose stop
```

彻底删除容器（保留数据库卷）：

```powershell
docker compose down
```

彻底清空（包含数据库数据）：

```powershell
docker compose down -v
```

### 方式二：本地开发启动（MySQL 用 Docker，前后端本机跑）

适合日常开发调试（改代码实时生效更快）。

#### 1) 先启动 MySQL 容器

```powershell
cd EcoSync
docker compose up -d mysql
```

#### 2) 启动后端（Spring Boot）

```powershell
cd EcoSync
.\mvnw.cmd spring-boot:run
```

启动后 Flyway 会自动执行迁移：
- `V1__init_schema.sql`：建表
- `V2__seed_data.sql`：测试数据

#### 3) 启动前端（Vite）

```powershell
cd EcoSync\ecosync-frontend
npm run dev
```

#### 4) 进入网站 / 文档

- 网站首页（前端 dev server）：<http://localhost:5173>
- 后端 API 文档（Knife4j）：<http://localhost:8080/doc.html>
- OpenAPI JSON：<http://localhost:8080/v3/api-docs>

#### 5) 停止服务

- 前端终端按 `Ctrl + C`
- 后端终端按 `Ctrl + C`
- MySQL 容器停止：

```powershell
cd EcoSync
docker compose stop mysql
```

---

## CI/CD（镜像驱动部署）

当前流水线采用“**构建镜像并推送 GHCR -> 部署机拉取指定 SHA 镜像**”的方式，避免部署机本地编译带来的不一致问题。

### 1) 流程概览

- CI（`ci.yml`）
  - 后端：`mvn compile` + `mvn package -DskipTests`
  - 前端：`npm ci` + `npm run build-only`
  - 集成测试：带 MySQL service 执行 `mvn test`
- CD（`cd.yml`）
  - 构建并推送两个镜像到 GHCR：
    - `ghcr.io/<owner>/ecosync-app:<sha>`
    - `ghcr.io/<owner>/ecosync-frontend:<sha>`
  - 通过跳板机 SSH 到目标机部署：
    - `docker compose pull`
    - `docker compose up -d`

### 2) 镜像与 Compose 对应关系

`docker-compose.yml` 里通过环境变量绑定镜像名和版本：

- `APP_IMAGE`（默认：`ghcr.io/jojozhu9/ecosync-app`）
- `FRONTEND_IMAGE`（默认：`ghcr.io/jojozhu9/ecosync-frontend`）
- `IMAGE_TAG`（默认：`latest`，CD 时会设置为当前 `github.sha`）

这意味着在部署机上，你可以直接用同一套 Compose 文件切换版本（例如回滚到某个历史 SHA）。

### 3) 部署机手动发布命令（与 CD 一致）

在部署机项目目录执行：

```bash
cd /opt/EcoSync
git pull
export APP_IMAGE=ghcr.io/<owner>/ecosync-app
export FRONTEND_IMAGE=ghcr.io/<owner>/ecosync-frontend
export IMAGE_TAG=<commit-sha>
docker compose pull
docker compose up -d
```

### 4) 常用运维命令

查看容器状态：

```bash
docker compose ps
```

查看日志：

```bash
docker compose logs -f app
docker compose logs -f frontend
docker compose logs -f mysql
```

回滚到上一版本（示例）：

```bash
export IMAGE_TAG=<old-commit-sha>
docker compose pull
docker compose up -d
```

### 5) 必要前置条件

- GitHub 仓库已开启 Packages 权限（GHCR）
- 部署机已登录 GHCR：`docker login ghcr.io`
- CD 里配置的 Secrets 可用：
  - `TARGET_HOST` / `TARGET_USER` / `TARGET_SSH_KEY`
  - `BASTION_HOST` / `BASTION_USER` / `BASTION_SSH_KEY`

---

## 测试

### 后端测试

后端测试分为两类：**单元测试**（Mockito，无需数据库）和**集成测试**（Spring Boot Test + 真实 MySQL）。

#### 运行全部后端测试

需要先启动 MySQL（Docker 方式即可）：

```powershell
# 1. 确保 MySQL 容器运行中
docker compose up -d mysql

# 2. 运行全部测试（指定数据库连接）
$env:SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3307/711ex?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="ecosync123"
.\mvnw.cmd test -B -Dspring.profiles.active=test
```

#### 仅运行单元测试（不需要 MySQL）

```powershell
.\mvnw.cmd test -B -Dtest="JwtUtilsTest,UserServiceImplTest,ShoppingCartServiceImplTest,OrderServiceImplTest"
```

#### 仅运行集成测试

```powershell
# 需要 MySQL 运行中
$env:SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3307/711ex?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="ecosync123"
.\mvnw.cmd test -B -Dtest="AuthRbacIntegrationTest,UserControllerIntegrationTest,OrderControllerIntegrationTest" -Dspring.profiles.active=test
```

#### 查看后端测试结果

控制台输出会直接显示通过/失败的汇总。如需查看详细报告：

```powershell
# Surefire XML 报告（CI 可解析）
cat target\surefire-reports\*.txt
```

#### 后端测试覆盖范围

| 测试类 | 类型 | 测试数 | 覆盖重点 |
|--------|------|--------|---------|
| `JwtUtilsTest` | 单元 | 4 | Token 生成/解析/过期/密钥不匹配 |
| `UserServiceImplTest` | 单元 | 7 | 登录/注册/密码加密/封禁用户 |
| `ShoppingCartServiceImplTest` | 单元 | 6 | 加购/合并数量/删除 |
| `OrderServiceImplTest` | 单元 | 15 | 折扣计算/库存/余额/核销 |
| `AuthRbacIntegrationTest` | 集成 | 8 | 401/403 权限/OPTIONS 预检/免认证路径 |
| `UserControllerIntegrationTest` | 集成 | 6 | 登录/注册/用户查询 API |
| `OrderControllerIntegrationTest` | 集成 | 6 | 下单/核销/订单查询 API |

### 前端测试

使用 Vitest 进行 API 层单元测试。

#### 运行前端测试

```powershell
cd ecosync-frontend
npx vitest run --reporter=verbose
```

也可以在开发时持续监听：

```powershell
cd ecosync-frontend
npx vitest
```

#### 前端测试覆盖范围

| 测试文件 | 测试数 | 覆盖重点 |
|----------|--------|---------|
| `src/api/__tests__/user.test.ts` | 4 | 登录/注册/查询用户 API |
| `src/api/__tests__/cart.test.ts` | 5 | 购物车增删改查 API |
| `src/api/__tests__/product.test.ts` | 4 | 临期商品/标准商品 API |

### CI 自动运行

推送到 main 分支或提 PR 时，GitHub Actions 会自动：

1. **后端**：`mvn test`（含 MySQL service container）
2. **前端**：`npx vitest run`（在 frontend-build job 中）

查看 CI 结果：GitHub 仓库 → Actions 选项卡 → 对应 workflow run。

---

## 数据库版本管理（Flyway）

- 迁移脚本目录：`src/main/resources/db/migration/`
- 命名规则：`V<版本号>__<描述>.sql`，例如 `V3__add_points_column.sql`
- 配置：见 `application.yml` 的 `spring.flyway`
  - `baseline-on-migrate: true`：如果库里已经有老表（例如手动跑过 `database/init_711_db.sql`），Flyway 不会炸，而是先 baseline 再增量迁移。
  - `baseline-version: 0`：全新库从头应用 V1 / V2。

> 如果你的开发库之前是手动跑 `database/init_711_db.sql` 初始化的，表和数据都已存在，想切到 Flyway 管理的**最干净做法**：
> ```sql
> DROP DATABASE 711ex;
> CREATE DATABASE 711ex DEFAULT CHARACTER SET utf8mb4;
> ```
> 然后重启应用，由 Flyway 重新建表+灌数据。

---

## 测试账号

> 下表的「密码」是**明文**，在登录接口里直接填这个明文即可。数据库 `users.password_hash` 字段存的是它的 MD5 值——登录时后端 (`UserServiceImpl`) 会把你提交的明文密码 MD5 后再与数据库比对。

### ADMIN（超级管理员，1 个）

| 用户名 | 密码 | 状态 | 说明 |
|---|---|---|---|
| `admin_super` | `1` | NORMAL | 系统超级管理员，不关联门店 |

### EMPLOYEE（门店员工，10 个，密码统一）

所有员工密码均为：**`1`**

| 用户名 | 关联门店 ID | 门店名 | 所在城市 |
|---|---|---|---|
| `emp_bj_01` | 1  | 7-ELEVEn Chaoyang Park        | Beijing   |
| `emp_bj_02` | 2  | 7-ELEVEn Guomao CBD           | Beijing   |
| `emp_bj_03` | 3  | 7-ELEVEn Zhongguancun         | Beijing   |
| `emp_bj_04` | 4  | 7-ELEVEn Wangfujing           | Beijing   |
| `emp_bj_05` | 5  | 7-ELEVEn Sanlitun             | Beijing   |
| `emp_sh_01` | 6  | 7-ELEVEn Lujiazui             | Shanghai  |
| `emp_sh_02` | 7  | 7-ELEVEn Xintiandi            | Shanghai  |
| `emp_gz_01` | 8  | 7-ELEVEn Tianhe Sports Center | Guangzhou |
| `emp_gz_02` | 9  | 7-ELEVEn Zhujiang New Town    | Guangzhou |
| `emp_sz_01` | 10 | 7-ELEVEn Futian CBD           | Shenzhen  |

### CONSUMER（消费者，5 个，密码统一）

所有消费者密码均为：**`1`**

| 用户名 | 状态 | 账户余额 (¥) | 备注 |
|---|---|---|---|
| `user_alice`   | NORMAL | 500.00 | 购物车里有 2 件商品，有一单 `PENDING` 待支付 |
| `user_bob`     | NORMAL |  20.50 | 有一单已完成 `COMPLETED` + 一单已取消 `CANCELLED` |
| `user_charlie` | NORMAL | 300.00 | 购物车里有 1 件商品，有一单 `AWAITING_PICKUP` 待提货 |
| `user_diana`   | NORMAL | 150.00 | 有一单 `PAID` 已支付未到待提货 |
| `user_eve`     | **BANNED** | 0.00 | 用于测试被封禁用户登录/下单被拦截的流程 |

---

## 已内置的测试订单（方便联调自提核销）

| 订单 ID | 消费者 | 取货门店 ID | 金额 (¥) | 取货码 | 状态 |
|---|---|---|---|---|---|
| 1 | user_bob     | 1  | 12.00 | `PICKUP-711-A1B2C3` | COMPLETED |
| 2 | user_charlie | 2  |  7.50 | `PICKUP-711-X9Y8Z7` | AWAITING_PICKUP |
| 3 | user_alice   | 6  | 10.50 | `PICKUP-711-M4N5P6` | PENDING |
| 4 | user_bob     | 3  |  4.00 | `PICKUP-711-Q1W2E3` | CANCELLED |
| 5 | user_diana   | 10 |  5.00 | `PICKUP-711-P9O8I7` | PAID |

---

## 目录结构速览

```
EcoSync/
├── src/main/java/com/example/bibilabo/    # 后端代码
│   ├── controller/                        # REST 接口
│   ├── service/                           # 业务层（登录、下单、核销等）
│   ├── mapper/                            # MyBatis Mapper
│   └── entity/                            # 实体类
├── src/main/resources/
│   ├── application.yml                    # 主配置（DB、Flyway、Knife4j）
│   ├── db/migration/                      # Flyway 迁移脚本
│   │   ├── V1__init_schema.sql
│   │   └── V2__seed_data.sql
│   └── database/
│       └── init_711_db.sql                # 手动初始化脚本（历史遗留，可选）
├── ecosync-frontend/                      # 前端项目
├── docker-compose.yml
└── pom.xml
```

---
