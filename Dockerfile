FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
RUN addgroup -S souklab && adduser -S -G souklab souklab
COPY --from=build --chown=souklab:souklab /app/target/*.jar app.jar
USER souklab
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
