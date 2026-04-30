# syntax=docker/dockerfile:1.7
# ============ Stage 1: Build ============
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /build

# 使用阿里云 Maven 镜像加速（Maven Central 在国内网络下可能极慢甚至超时）
RUN mkdir -p /root/.m2 && cat > /root/.m2/settings.xml <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>aliyun-public</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Public Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests -B

# ============ Stage 2: Runtime ============
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
