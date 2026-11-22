FROM maven:3.10.0-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src/ src/
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

ENV SERVER_ADDRESS=0.0.0.0
ENV PORT=8080

RUN useradd --system --uid 10001 appuser && mkdir -p /app/data
COPY --from=build /workspace/target/campus-lost-found-web-0.0.1-SNAPSHOT.jar /app/app.jar
RUN chown appuser:appuser /app/app.jar /app/data
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
