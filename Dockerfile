# Build Java backend and Angular frontend
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# Required by ui's prebuild column check
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 \
    && rm -rf /var/lib/apt/lists/*

# Include all Maven modules and scripts/check_columns.py
COPY . .

RUN mvn -B -DskipTests package

# Run the packaged application
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
