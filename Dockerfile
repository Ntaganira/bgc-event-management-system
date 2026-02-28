# BGC Event Management System - Dockerfile
# Author: NTAGANIRA Heritier | Date: 2026-02-27

# ------------------------
# Builder stage
# ------------------------
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Install Maven and build the project
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

# ------------------------
# Production stage
# ------------------------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port and set Spring profile
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]