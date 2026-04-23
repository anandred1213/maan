# ---------- Stage 1: Build ----------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Create dedicated maven repo (avoids corruption issues)
RUN mkdir -p /root/.m2

# Copy pom.xml first (for caching)
COPY pom.xml .

# Download dependencies with retry + checksum
RUN mvn -B -e -C dependency:go-offline \
    -Dmaven.wagon.http.retryHandler.count=5

# Copy source code
COPY src ./src

# Build application
RUN mvn -B clean package -DskipTests


# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install curl (required for healthcheck)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create non-root user (security)
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Expose Eureka port
EXPOSE 8761

# Healthcheck (Eureka UI)
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8761 || exit 1

# Run application with container-aware JVM
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]