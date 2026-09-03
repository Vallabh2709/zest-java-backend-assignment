# =========================================================
# Stage 1: Build the application
# =========================================================

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first
# This allows Docker to cache dependencies between builds.
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copy application source
COPY src ./src

# Build the Spring Boot application
RUN mvn clean package -DskipTests


# =========================================================
# Stage 2: Run the application
# =========================================================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the generated JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Application port
EXPOSE 8080

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]