# Multi-stage build for Spring Boot application with Gradle
FROM openjdk:21-jdk-slim AS builder

WORKDIR /app

# Copy Gradle build files
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src

# Make gradlew executable and build
RUN chmod +x gradlew && \
    ./gradlew build --no-daemon -x test

FROM openjdk:21-jre-slim AS runtime

# Create non-root user
RUN addgroup --system chimera && adduser --system --group chimera

WORKDIR /app

# Copy built application
COPY --from=builder /app/build/libs/chimera-backend.jar app.jar

# Set ownership
RUN chown -R chimera:chimera /app
USER chimera

# Expose port
EXPOSE 8080

# Install curl for health checks
USER root
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
USER chimera

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application with Railway-specific profile
ENTRYPOINT ["java", "-Dspring.profiles.active=railway", "--enable-preview", "-jar", "app.jar"]