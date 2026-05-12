# syntax=docker/dockerfile:1.7
# ============ Stage 1: Build Vue SPA ============
FROM node:22-alpine AS builder
WORKDIR /build

RUN npm config set registry https://registry.npmmirror.com

COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci

COPY . .
RUN npm run build-only

# ============ Stage 2: Serve via Nginx ============
FROM nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80
