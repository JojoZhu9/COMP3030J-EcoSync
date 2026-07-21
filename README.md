<p align="right"><strong>English</strong> | <a href="./README_CN.md">简体中文</a></p>

# EcoSync: Near-Expiry Inventory and Ordering Platform

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![MySQL 8](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker Compose](https://img.shields.io/badge/Docker%20Compose-supported-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![License: No license](https://img.shields.io/badge/License-No%20license-lightgrey)](#project-status)

EcoSync is a group course project for COMP3030J. The team built this full-stack platform to manage and sell near-expiry convenience-store products through coordinated administrator, store-employee, and consumer workflows.

## Project Status

This repository is a course-project snapshot maintained for demonstration, review, and learning contributions. It is not a production service, and its deployment workflow is maintainer-controlled.

## What It Demonstrates

- **Administrators** manage stores, accounts, standard products, expiring inventory, and sales analytics.
- **Store employees** record store inventory, maintain their profile, and support local stock workflows.
- **Consumers** browse discounted near-expiry products, manage a cart, place orders, track status, and verify pickup with a code.
- The stack combines Spring Boot, Vue 3, MySQL/Flyway, MinIO object storage, Docker Compose, Nginx, and GitHub Actions.

## Quick Start

### Docker Compose

```powershell
git clone https://github.com/JojoZhu9/COMP3030J-EcoSync.git
cd COMP3030J-EcoSync
$env:DOCKER_BUILDKIT=1
docker compose up -d --build
```

- Web app: <http://localhost>
- API documentation: <http://localhost/doc.html>
- OpenAPI JSON: <http://localhost/v3/api-docs>
- MinIO console: <http://localhost:9001>

Useful commands:

```powershell
docker compose ps
docker compose logs -f
docker compose down
```

### Local Development

Start MySQL, then run the backend and frontend in separate terminals:

```powershell
docker compose up -d mysql
.\mvnw.cmd spring-boot:run
```

```powershell
cd ecosync-frontend
npm install
npm run dev
```

- Frontend development server: <http://localhost:5173>
- Backend API documentation: <http://localhost:8080/doc.html>

## Demo Accounts

These seeded accounts are for local demos and tests only. All use password `1`.

| Role | Username examples | Workflow |
| --- | --- | --- |
| Administrator | `admin_super` | Manage stores, users, inventory, and analytics. |
| Store employee | `emp_bj_01`, `emp_sh_01`, `emp_sz_01` | Record and manage store-level inventory. |
| Consumer | `user_alice`, `user_bob`, `user_charlie`, `user_diana` | Browse, order, track, and collect products. |
| Banned consumer | `user_eve` | Exercise blocked-account behavior. |

## Validation

Backend integration tests need MySQL to be available on the configured test datasource:

```powershell
docker compose up -d mysql
.\mvnw.cmd test -B
```

Frontend validation:

```powershell
npm --prefix ecosync-frontend install
npm --prefix ecosync-frontend run build
npm --prefix ecosync-frontend exec -- vitest run --reporter=verbose
```

## Deployment Boundary

GitHub Actions compiles/packages the backend and builds the frontend. The repository's CD workflow builds GHCR images and deploys only through maintainer-managed infrastructure and secrets. Contributors should not add credentials to issues, pull requests, documentation, or source files, and should not treat the demo stack as a production environment.

## Contributing and Security

EcoSync welcomes focused, reviewable improvements that respect its shared course-project authorship. Read [CONTRIBUTING.md](./CONTRIBUTING.md) before opening a pull request and [SECURITY.md](./SECURITY.md) before reporting a vulnerability.

## Project Structure

```text
COMP3030J-EcoSync/
|-- src/                  # Spring Boot backend and tests
|-- ecosync-frontend/     # Vue 3 frontend and tests
|-- docker-compose.yml    # Local multi-service stack
|-- .github/workflows/    # CI/CD definitions
`-- docs/                 # Project documentation
```
