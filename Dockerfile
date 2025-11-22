# 🔹 1. Stage: build Maven project
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy Maven files
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src

# Build the Spring Boot application
RUN mvn -q clean package -DskipTests

# 🔹 2. Stage: run JAR using a small JRE image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from first stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
