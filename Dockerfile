FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget curl
RUN addgroup -S souklab && adduser -S -G souklab souklab
RUN mkdir -p /app/storage/uploads /app/storage/thumbnails /app/storage/indexes && \
    chown -R souklab:souklab /app
COPY --from=build --chown=souklab:souklab /app/target/*.jar app.jar
USER souklab
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health/readiness | grep -q 'UP' || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
