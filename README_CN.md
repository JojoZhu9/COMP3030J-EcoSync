<p align="right"><a href="./README.md">English</a> | <strong>简体中文</strong></p>

# EcoSync：临期商品库存与订购平台

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![MySQL 8](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker Compose](https://img.shields.io/badge/Docker%20Compose-supported-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![License: No license](https://img.shields.io/badge/License-No%20license-lightgrey)](#项目状态)

EcoSync 是 COMP3030J 的小组课程项目。团队共同构建了这个全栈平台，用于通过管理员、门店员工和消费者协作流程管理并销售便利店临期商品。

## 项目状态

本仓库是用于演示、评审和学习贡献的课程项目快照，并非生产服务；部署流程仅由维护者管理。

## 项目功能

- **管理员**管理门店、账号、标准商品、临期库存和销售分析。
- **门店员工**录入门店库存、维护个人资料并支持本地库存流程。
- **消费者**浏览折扣临期商品、管理购物车、下单、跟踪订单状态，并通过取货码完成核销。
- 技术栈包括 Spring Boot、Vue 3、MySQL/Flyway、MinIO 对象存储、Docker Compose、Nginx 和 GitHub Actions。

## 快速启动

### Docker Compose

```powershell
git clone https://github.com/JojoZhu9/COMP3030J-EcoSync.git
cd COMP3030J-EcoSync
$env:DOCKER_BUILDKIT=1
docker compose up -d --build
```

- Web 应用：<http://localhost>
- API 文档：<http://localhost/doc.html>
- OpenAPI JSON：<http://localhost/v3/api-docs>
- MinIO 控制台：<http://localhost:9001>

常用命令：

```powershell
docker compose ps
docker compose logs -f
docker compose down
```

### 本地开发

先启动 MySQL，再在不同终端启动后端和前端：

```powershell
docker compose up -d mysql
.\mvnw.cmd spring-boot:run
```

```powershell
cd ecosync-frontend
npm install
npm run dev
```

- 前端开发服务器：<http://localhost:5173>
- 后端 API 文档：<http://localhost:8080/doc.html>

## 演示账号

以下为本地演示和测试使用的预置账号，密码均为 `1`。

| 角色 | 用户名示例 | 对应流程 |
| --- | --- | --- |
| 管理员 | `admin_super` | 管理门店、用户、库存和分析数据。 |
| 门店员工 | `emp_bj_01`、`emp_sh_01`、`emp_sz_01` | 录入并管理门店级库存。 |
| 消费者 | `user_alice`、`user_bob`、`user_charlie`、`user_diana` | 浏览、下单、跟踪和取货。 |
| 被禁用消费者 | `user_eve` | 验证受限账号行为。 |

## 验证

后端集成测试需要 MySQL 在已配置的测试数据源上可用：

```powershell
docker compose up -d mysql
.\mvnw.cmd test -B
```

前端验证：

```powershell
npm --prefix ecosync-frontend install
npm --prefix ecosync-frontend run build
npm --prefix ecosync-frontend exec -- vitest run --reporter=verbose
```

## 部署边界

GitHub Actions 会编译/打包后端并构建前端。仓库中的 CD 工作流仅通过维护者管理的基础设施和密钥构建 GHCR 镜像并部署。贡献者不得在 issue、PR、文档或源代码中添加凭据，也不得将演示环境视为生产环境。

## 贡献与安全

欢迎提交尊重小组课程项目共同署名、范围清晰且便于评审的改进。提交 PR 前请阅读 [CONTRIBUTING.md](./CONTRIBUTING.md)；报告安全问题前请阅读 [SECURITY.md](./SECURITY.md)。

## 项目结构

```text
COMP3030J-EcoSync/
|-- src/                  # Spring Boot 后端与测试
|-- ecosync-frontend/     # Vue 3 前端与测试
|-- docker-compose.yml    # 本地多服务环境
|-- .github/workflows/    # CI/CD 定义
`-- docs/                 # 项目文档
```
