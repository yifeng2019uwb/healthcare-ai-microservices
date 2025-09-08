# Multi-stage build for Spring Boot application
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom files first for better layer caching
COPY services/shared/pom.xml services/shared/
COPY services/patient-service/pom.xml services/patient-service/

# Copy source code
COPY services/shared/src services/shared/src
COPY services/patient-service/src services/patient-service/src

# Build the shared module first
RUN mvn -f services/shared/pom.xml clean install -DskipTests

# Build the patient service
RUN mvn -f services/patient-service/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/services/patient-service/target/patient-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Set JVM options for container
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
