# -----------------------------
# Stage 1: Build
# -----------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only pom.xml first (for dependency caching)
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests


# -----------------------------
# Stage 2: Run
# -----------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built JAR from previous stage
COPY --from=build /app/target/*.jar app.jar

# Install curl (required for healthcheck)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# ✅ FIX: Create log directory (prevents crash)
RUN mkdir -p /app/logs && chmod -R 777 /app/logs

# Create non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Set ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]