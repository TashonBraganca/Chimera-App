# Railway Deployment - Java 21 Spring Boot with Gradle
# This Dockerfile builds from the backend directory
FROM amazoncorretto:21-alpine AS builder

# Install necessary build tools
RUN apk add --no-cache bash

WORKDIR /app

# Copy Gradle wrapper and build files (from current backend directory)
COPY gradlew ./
COPY gradle ./gradle  
COPY build.gradle settings.gradle ./
COPY src ./src

# Make gradlew executable and build
RUN chmod +x gradlew && \
    ./gradlew build --no-daemon -x test --stacktrace

# Runtime stage with minimal image
FROM amazoncorretto:21-alpine AS runtime

# Install curl for health checks
RUN apk add --no-cache curl bash

# Create non-root user
RUN addgroup -S chimera && adduser -S chimera -G chimera

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Set ownership and switch to non-root user
RUN chown chimera:chimera app.jar
USER chimera

# Expose port for Railway
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run with Railway profile using Railway-specific application class
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=railway", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dserver.port=${PORT:-8080}", \
    "-Dspring.main.banner-mode=console", \
    "-jar", "app.jar"]