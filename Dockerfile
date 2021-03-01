# Stage 1: Build
FROM maven:3.6-openjdk-11 AS build

WORKDIR /app

# Copy pom.xml truoc de tan dung Docker cache cho dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code va build
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime
FROM openjdk:11-jre-slim

WORKDIR /app

# Copy file JAR tu build stage
COPY --from=build /app/target/*.jar app.jar

# Tao user khong phai root de chay ung dung
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
