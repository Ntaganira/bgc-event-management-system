# docker/Dockerfile
# Multi-stage build for smaller final image

# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /build

# Copy pom.xml and download dependencies (caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /build/target/*.jar app.jar

# Create directories for logs and uploads
RUN mkdir -p logs uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]