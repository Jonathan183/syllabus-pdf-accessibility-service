# ----------------------------
# Build stage
# ----------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Build a Spring Boot executable jar
RUN mvn -q -DskipTests clean package

# ----------------------------
# Run stage
# ----------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy ONLY the executable jar (exclude *.original)
# This finds the first jar that is not ".original"
RUN echo "Will copy executable jar from build stage..."

COPY --from=build /app/target/*.jar /app/target/

# Remove any non-executable original jar, then pick the remaining jar
RUN rm -f /app/target/*.original || true && \
    ls -la /app/target && \
    JAR_PATH=$(ls /app/target/*.jar | head -n 1) && \
    echo "Using jar: $JAR_PATH" && \
    cp "$JAR_PATH" /app/app.jar && \
    ls -la /app/app.jar

# Fonts (optional but recommended)
COPY src/main/resources/fonts /app/fonts
RUN mkdir -p /app/fonts

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]