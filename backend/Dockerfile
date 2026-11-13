FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y --no-install-recommends maven \
    && mvn -q -DskipTests package \
    && apt-get purge -y maven && rm -rf /var/lib/apt/lists/*

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /src/target/beatsync-api-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8091
ENV SERVER_PORT=8091
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
