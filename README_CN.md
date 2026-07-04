<p align="right"><a href="./README.md#english">English</a> | <strong>中文</strong> | <a href="./README.md#zh-cn">返回仓库首页中文区</a></p>

# EcoSync - 7-ELEVEn 临期商品平台

> 说明：GitHub 仓库首页只会自动渲染根目录的 `README.md`。如果希望在仓库首页内切换语言，请使用 [README.md 的中文区](./README.md#zh-cn)。本文件作为中文独立版保留。

EcoSync 是一个面向便利店临期商品管理与销售的全栈平台，覆盖门店管理、标准商品字典、临期库存上架、消费者下单、到店自提核销和数据分析。

## 功能亮点

- 支持管理员、门店员工、消费者三类角色流程。
- 支持标准商品字典和门店级临期商品库存管理。
- 支持购物车、下单、订单状态追踪和自提码核销。
- 支持销售趋势、折扣分布、热门商品等分析数据。
- Vue 前端已接入中英文 i18n 语言文件。
- 使用 Flyway 管理 MySQL 表结构迁移和演示数据。
- 提供 Docker Compose 部署方案，包含 MySQL、Spring Boot、Vue、MinIO 和 Nginx gateway。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.2、Java 17、MyBatis、JWT |
| 前端 | Vue 3、TypeScript、Vite、Element Plus、Pinia、vue-i18n |
| 数据库 | MySQL 8、Flyway |
| 对象存储 | MinIO |
| 接口文档 | Knife4j / OpenAPI 3 |
| DevOps | Docker Compose、Nginx gateway、GitHub Actions、GHCR |
| 测试 | JUnit、Mockito、Spring Boot Test、Vitest、Playwright |

## 快速启动

### Docker Compose 一键启动

```powershell
git clone https://github.com/JojoZhu9/COMP3030J-EcoSync.git
cd COMP3030J-EcoSync
$env:DOCKER_BUILDKIT=1
docker compose up -d --build
```

访问：

- 前端网站：<http://localhost>
- API 文档：<http://localhost/doc.html>
- OpenAPI JSON：<http://localhost/v3/api-docs>
- MinIO 控制台：<http://localhost:9001>

常用命令：

```powershell
docker compose ps
docker compose logs -f
docker compose stop
docker compose down
docker compose down -v
```

### 本地开发

```powershell
docker compose up -d mysql
.\mvnw.cmd spring-boot:run
```

另开终端启动前端：

```powershell
cd ecosync-frontend
npm install
npm run dev
```

访问：

- 前端 dev server：<http://localhost:5173>
- 后端 API 文档：<http://localhost:8080/doc.html>

## 测试账号

以下演示账号密码均为 `1`。

| 角色 | 用户名示例 | 说明 |
| --- | --- | --- |
| 管理员 | `admin_super` | 系统管理员 |
| 门店员工 | `emp_bj_01`、`emp_sh_01`、`emp_sz_01` | 关联不同演示门店 |
| 消费者 | `user_alice`、`user_bob`、`user_charlie`、`user_diana` | 普通消费者账号 |
| 封禁消费者 | `user_eve` | 用于测试封禁账号流程 |

## 测试

后端测试：

```powershell
.\mvnw.cmd test -B
```

如需显式指定测试数据库连接：

```powershell
docker compose up -d mysql
$env:SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/711ex?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="ecosync123"
.\mvnw.cmd test -B -Dspring.profiles.active=test
```

前端测试：

```powershell
cd ecosync-frontend
npm install
npx vitest run --reporter=verbose
```

## 部署说明

- CI 会编译后端、构建前端，并运行后端/前端测试。
- CD 会构建后端和前端镜像，推送到 GHCR，再通过指定 commit SHA 的镜像完成部署。
- 运行镜像由 `APP_IMAGE`、`FRONTEND_IMAGE`、`IMAGE_TAG` 控制。

手动部署模式：

```bash
export APP_IMAGE=ghcr.io/<owner>/ecosync-app
export FRONTEND_IMAGE=ghcr.io/<owner>/ecosync-frontend
export IMAGE_TAG=<commit-sha>
docker compose pull app frontend
docker compose up -d --remove-orphans
docker compose up -d --force-recreate frontend gateway
```

## 项目结构

```text
COMP3030J-EcoSync/
|-- src/main/java/com/example/bibilabo/
|   |-- controller/
|   |-- service/
|   |-- mapper/
|   `-- entity/
|-- src/main/resources/
|   |-- application.yml
|   `-- db/migration/
|-- ecosync-frontend/
|   |-- src/
|   |-- package.json
|   `-- vite.config.ts
|-- docker-compose.yml
|-- Dockerfile
`-- pom.xml
```
