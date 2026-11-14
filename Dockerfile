# ==============================================================================
# Multi-stage Dockerfile for Cosecha en Cope
# 1. Frontend Builder (Node.js 22 + Angular 20)
# 2. Backend Builder (Java 17 + Maven 3 + Spring Boot 3.5.2)
# 3. Production Runtime (Eclipse Temurin JRE 17 Alpine, non-root user)
# ==============================================================================

# --- Stage 1: Build Angular SPA ---
FROM node:22-alpine AS frontend-builder
WORKDIR /app/frontend/cosechaencope-frontend

COPY frontend/cosechaencope-frontend/package*.json ./
RUN npm ci --prefer-offline --no-audit

COPY frontend/cosechaencope-frontend/ ./
RUN mkdir -p /app/backend/cosechaencope/src/main/resources/static/app
RUN npm run build

# --- Stage 2: Build Spring Boot JAR ---
FROM eclipse-temurin:17-jdk-alpine AS backend-builder
WORKDIR /app/backend/cosechaencope

COPY backend/cosechaencope/mvnw backend/cosechaencope/mvnw.cmd backend/cosechaencope/pom.xml ./
COPY backend/cosechaencope/.mvn .mvn
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

COPY backend/cosechaencope/src src
COPY --from=frontend-builder /app/backend/cosechaencope/src/main/resources/static/app src/main/resources/static/app

RUN ./mvnw clean package -DskipTests

# --- Stage 3: Minimal Secure Production Runtime ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

COPY --from=backend-builder /app/backend/cosechaencope/target/*.jar app.jar

ENV SERVER_PORT=8081
EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
