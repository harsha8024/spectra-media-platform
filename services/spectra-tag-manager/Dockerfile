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

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

ENV WAIT_VERSION 2.7.2
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/$WAIT_VERSION/wait /wait
RUN chmod +x /wait
# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]