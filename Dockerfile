imp# Build stage: use Maven to build account-service
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy parent POM and all modules
COPY pom.xml .

# Copy all module POMs and source
COPY account-service account-service
COPY transaction-service transaction-service
COPY notification-service notification-service

# Build only account-service (and its dependencies from parent)
RUN mvn clean package -pl account-service -am -DskipTests -q

# Run stage: slim JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Render sets PORT; Spring Boot will read it if we pass it
ENV PORT=8081
EXPOSE 8081

COPY --from=build /app/account-service/target/account-service-*.jar app.jar

CMD ["sh", "-c", "java -jar -Dserver.port=${PORT} app.jar"]
