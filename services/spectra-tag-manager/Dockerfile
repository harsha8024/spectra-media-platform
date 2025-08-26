# Build stage
FROM maven:3.8-eclipse-temurin-17 AS build
WORKDIR /app
# Copy pom.xml first to leverage Docker cache
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline
# Copy source code
COPY src ./src
# Build the application
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar
# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]