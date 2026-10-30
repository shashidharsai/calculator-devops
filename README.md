# Calculator DevOps

A Calculator REST API (plain Java 21, Maven, JDK built-in HTTP server) with a complete CI/CD pipeline:
**GitHub -> Jenkins -> Maven build/test -> Docker image -> Docker Hub -> Docker Compose (Nginx + 2 API replicas)**.

## API

| Operation | Request |
| --- | --- |
| Add | `GET /api/v1/add?a=2&b=3` |
| Subtract | `GET /api/v1/subtract?a=10&b=4` |
| Multiply | `GET /api/v1/multiply?a=6&b=7` |
| Divide | `GET /api/v1/divide?a=10&b=4` |
| Health | `GET /actuator/health` |

Success response:

```json
{ "operation": "add", "a": 2, "b": 3, "result": 5 }
```

Error response (HTTP 400) for missing/invalid parameters and division by zero:

```json
{ "status": 400, "error": "Bad Request", "message": "Division by zero is not allowed" }
```

## Prerequisites

Java 21, Maven 3.8+, Docker with the Compose plugin.

## Build

```bash
mvn clean package
```

## Test

```bash
mvn test
```

JUnit XML reports are written to `target/surefire-reports/`.

## Run locally (without Docker)

```bash
java -jar target/calculator.jar
curl "http://localhost:8080/api/v1/add?a=2&b=3"
```

## Run with Docker

```bash
docker build -t calculator-api .
docker run -d --name calculator -p 8080:8080 calculator-api
curl "http://localhost:8080/actuator/health"
```

## Run the full stack with Docker Compose

```bash
docker compose up -d --build
curl "http://localhost:8081/api/v1/divide?a=10&b=4"
curl "http://localhost:8081/actuator/health"
docker compose down
```

Nginx listens on port **8081** and load-balances across two calculator replicas.

## CI/CD pipeline

The `Jenkinsfile` defines these stages: Checkout -> Build -> Test -> Docker Build -> Push -> Deploy.
A push to `main` triggers it (GitHub webhook, with Poll SCM as a fallback).
Docker Hub credentials live in the Jenkins credentials store (ID `dockerhub-creds`), never in the repo.
