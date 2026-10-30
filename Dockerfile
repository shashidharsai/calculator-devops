# ---------- Stage 1: build the JAR (Maven + JDK) ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only pom.xml first so the dependency layer is cached
# and only re-downloaded when pom.xml changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Now copy the source and build (tests already run in Jenkins)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: slim runtime (JRE only) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-root user
RUN addgroup -S app && adduser -S app -G app

# Copy ONLY the built JAR from the build stage
COPY --from=build --chown=app:app /app/target/calculator.jar app.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
