
# Use Java 17 base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven files and source code
COPY pom.xml .
COPY src ./src

# Build the project
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# Copy the built JAR
COPY target/subtrak-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables (these will be configured in Render)
ENV DB_URL=${DB_URL}
ENV DB_USER=${DB_USER}
ENV DB_PASS=${DB_PASS}
ENV JWT_SECRET=${JWT_SECRET}

# Start the app
ENTRYPOINT ["java","-jar","app.jar"]
