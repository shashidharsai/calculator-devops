# Calculator DevOps

A Calculator REST API built using **plain Java 21 and Maven**, using Java's built-in HTTP server. The project demonstrates a CI/CD workflow using GitHub, Jenkins, Docker, Docker Hub, Docker Compose, and Nginx.

The intended workflow is:

**GitHub → Jenkins → Maven Build & Test → Docker Image → Docker Hub → Docker Compose (Nginx + Two API Replicas)**

## Table of Contents

- [Overview](#overview)
- [Technologies Used](#technologies-used)
- [Features](#features)
- [REST API](#rest-api)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Build and Test](#build-and-test)
- [Run Locally](#run-locally)
- [Run with Docker](#run-with-docker)
- [Run with Docker Compose](#run-with-docker-compose)
- [CI/CD Pipeline](#cicd-pipeline)
- [Jenkins Configuration](#jenkins-configuration)
- [Verification](#verification)
- [Security](#security)
- [Future Improvements](#future-improvements)

## Overview

The project implements a calculator REST API that supports arithmetic operations and a health-check endpoint.

It also demonstrates how to automate application testing, packaging, container image creation, image publishing, and deployment using DevOps tools.

Jenkins executes the pipeline defined in the `Jenkinsfile`, while Docker packages the application into a container. Docker Compose manages the application services, and Nginx acts as a reverse proxy and load balancer for two calculator API replicas.

## Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | REST API implementation |
| JDK HTTP Server | HTTP request handling |
| Maven | Dependency management, testing, and packaging |
| JUnit | Automated testing |
| Git | Version control |
| GitHub | Source code hosting |
| Jenkins | CI/CD pipeline automation |
| Docker | Application containerization |
| Docker Hub | Container image registry |
| Docker Compose | Multi-container application management |
| Nginx | Reverse proxy and load balancing |
| Ubuntu | Development and execution environment |
| VirtualBox | Virtual machine environment |

## Features

- REST API for addition, subtraction, multiplication, and division.
- Health-check endpoint.
- Input validation and HTTP 400 error responses.
- Automated tests using Maven and JUnit.
- Multi-stage Docker build to separate build and runtime environments.
- Jenkins pipeline for automated building and testing.
- Docker image creation and publishing.
- Docker Compose configuration for running multiple services.
- Nginx load balancing across two calculator API replicas.

The deployment and image-publishing features depend on the corresponding Jenkins credentials and Docker Compose configuration being set up correctly.

## REST API

The API listens on port `8080` when running directly or when exposed through the appropriate Docker port mapping.

### Endpoints

| Operation | HTTP Request | Example Result |
|---|---|---|
| Addition | `GET /api/v1/add?a=2&b=3` | `5` |
| Subtraction | `GET /api/v1/subtract?a=10&b=4` | `6` |
| Multiplication | `GET /api/v1/multiply?a=6&b=7` | `42` |
| Division | `GET /api/v1/divide?a=10&b=4` | `2.5` |
| Health check | `GET /actuator/health` | Health status |

### Example Success Response

Request:

```http
GET /api/v1/add?a=2&b=3
```

Response:

```json
{
  "operation": "add",
  "a": 2,
  "b": 3,
  "result": 5
}
```

### Example Error Response

Missing or invalid parameters and division by zero should return HTTP `400 Bad Request`.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Division by zero is not allowed"
}
```

The exact response depends on the validation and error-handling implementation in the application.

## Prerequisites

Install or configure the following tools:

- Java Development Kit (JDK) 21
- Apache Maven 3.8 or later
- Git
- Docker Engine
- Docker Compose plugin
- Jenkins, for CI/CD automation
- A Docker Hub account, if publishing images

## Project Structure

```text
calculator-devops/
├── src/
│   ├── main/
│   │   └── java/          # Application source code
│   └── test/
│       └── java/          # Automated tests
├── nginx/                 # Nginx configuration
├── pom.xml                # Maven configuration
├── Dockerfile             # Docker image build instructions
├── docker-compose.yml     # Multi-container configuration
├── Jenkinsfile            # Jenkins pipeline definition
├── .gitignore             # Git exclusions
├── .dockerignore          # Docker build exclusions
└── README.md              # Project documentation
```

The exact directory structure depends on the current implementation.

## Build and Test

### Build the application

From the project root directory, run:

```bash
mvn clean package
```

Maven compiles the Java source code, runs the configured tests, and packages the application.

The expected JAR location is:

```text
target/calculator.jar
```

### Run the tests

```bash
mvn test
```

JUnit XML test reports are written to:

```text
target/surefire-reports/
```

To inspect the available reports:

```bash
ls -la target/surefire-reports/
```

## Run Locally

After building the project, start the application:

```bash
java -jar target/calculator.jar
```

Keep the application running in that terminal.

Open another terminal and test the API:

```bash
curl "http://localhost:8080/api/v1/add?a=2&b=3"
```

Test the health endpoint:

```bash
curl "http://localhost:8080/actuator/health"
```

Stop the application using `Ctrl+C` when finished.

## Run with Docker

### Build the Docker image

From the project root:

```bash
docker build -t calculator-api .
```

### Start the container

```bash
docker run -d --name calculator -p 8080:8080 calculator-api
```

### Test the API

```bash
curl "http://localhost:8080/api/v1/add?a=2&b=3"
```

Check the health endpoint:

```bash
curl "http://localhost:8080/actuator/health"
```

### Inspect logs

```bash
docker logs calculator
```

### Stop and remove the container

```bash
docker stop calculator
docker rm calculator
```

## Run with Docker Compose

Docker Compose starts the configured application services, including Nginx and the calculator API replicas.

### Start the full stack

```bash
docker compose up -d --build
```

### Check running services

```bash
docker compose ps
```

### Test the API through Nginx

Nginx is configured to listen on port `8081`.

Test division:

```bash
curl "http://localhost:8081/api/v1/divide?a=10&b=4"
```

Test the health endpoint:

```bash
curl "http://localhost:8081/actuator/health"
```

### Inspect service logs

```bash
docker compose logs
```

To inspect a particular service:

```bash
docker compose logs nginx
```

Use the actual service names defined in `docker-compose.yml`.

### Stop the full stack

```bash
docker compose down
```

### Architecture

```text
                   Client
                     |
                     v
               Nginx :8081
                     |
              Load Balancer
                /       \
               v         v
        Calculator 1   Calculator 2
          :8080           :8080
               \         /
                \       /
             Calculator REST API
```

Nginx distributes requests between the two API replicas according to the configured load-balancing strategy.

## CI/CD Pipeline

The `Jenkinsfile` defines the automated workflow for building and delivering the application.

### Pipeline stages

1. **Checkout:** Retrieve source code from GitHub.
2. **Build:** Compile the Java application using Maven.
3. **Test:** Run automated tests.
4. **Docker Build:** Create the Docker image.
5. **Push:** Authenticate with Docker Hub and publish the image.
6. **Deploy:** Run the configured deployment workflow using Docker Compose.

The push and deploy stages require valid configuration, credentials, and appropriate access to the deployment environment.

### CI/CD workflow

```text
Developer
    |
    v
GitHub Repository
    |
    v
Jenkins Pipeline
    |
    +--> Checkout
    |
    +--> Maven Build
    |
    +--> Automated Tests
    |
    +--> Docker Image Build
    |
    +--> Push to Docker Hub
    |
    +--> Deploy with Docker Compose
    |
    v
Nginx + Two Calculator API Replicas
```

## Jenkins Configuration

To configure Jenkins:

1. Install and start Jenkins.
2. Create a Pipeline job named `calculator-devops`.
3. Select **Pipeline script from SCM**.
4. Select **Git** as the source control management system.
5. Set the repository URL to:

   ```text
   https://github.com/shashidharsai/calculator-devops.git
   ```

6. Set the branch specifier to `*/main`.
7. Set the script path to `Jenkinsfile`.
8. Save the configuration.
9. Click **Build Now** to execute the pipeline.

### Automatic builds

SCM polling can be configured under the job's **Build Triggers** section.

For example:

```text
H/2 * * * *
```

This checks the repository approximately every two minutes. A build runs when Jenkins detects relevant changes.

A GitHub webhook can also trigger builds when changes are pushed, provided Jenkins is reachable from GitHub and the webhook is configured correctly.

### Docker Hub credentials

Configure Docker Hub credentials in the Jenkins credentials store.

The credential ID used by the pipeline is:

```text
dockerhub-creds
```

The `Jenkinsfile` should reference this credential ID rather than embedding a username, password, or access token in the source code.

## Verification

### Verify the Jenkins pipeline

Open the latest Jenkins build and inspect **Console Output**.

A successful pipeline should end with:

```text
Finished: SUCCESS
```

### Verify the Docker image

```bash
docker images calculator-api
```

Use the image name configured in your Docker build and pipeline.

### Verify the running services

```bash
docker compose ps
```

Confirm that the expected services are running.

### Verify the API

```bash
curl "http://localhost:8081/api/v1/add?a=2&b=3"
```

A successful request should return a JSON response containing the operation and result.

## Security

- Store Docker Hub credentials in Jenkins Credentials.
- Never commit passwords, access tokens, or secrets to GitHub.
- Use a `.gitignore` file to exclude local configuration and generated files.
- Use a `.dockerignore` file to avoid copying unnecessary files into the Docker build context.
- Run containers with only the permissions they require.
- Restrict access to Jenkins and the Docker daemon in shared environments.

## Future Improvements

- Add GitHub webhook-based triggering.
- Add automated integration tests for the REST API.
- Publish versioned Docker images.
- Deploy automatically after successful builds.
- Add container health checks and monitoring.
- Add deployment rollback and failure-recovery mechanisms.
- Introduce security scanning for dependencies and Docker images.

## Author

**Shashidhar Sai**

GitHub: [shashidharsai](https://github.com/shashidharsai)

Repository: [calculator-devops](https://github.com/shashidharsai/calculator-devops)
