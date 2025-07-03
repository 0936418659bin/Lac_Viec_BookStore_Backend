# Stage 1: Build jar
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy all files
COPY . .

# Build the jar file, skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Run app
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
