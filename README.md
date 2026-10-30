# Calculator DevOps Project

A Java-based Calculator REST API built and tested using Maven, containerized with Docker, and integrated with Jenkins for Continuous Integration (CI). Docker Compose and Nginx are used to run and route requests to the application.

## Project Overview

This project demonstrates how DevOps tools can work together to build, test, package, and run a Java application.

The project uses Jenkins to automate the build and testing process, Docker to package the application into a container image, and Docker Compose with Nginx to run and access the application.

## Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | Application development |
| Maven | Build management and testing |
| Git | Version control |
| GitHub | Source code hosting |
| Jenkins | CI pipeline automation |
| Docker | Application containerization |
| Docker Compose | Running multiple containers |
| Nginx | Routing requests to the calculator service |
| Ubuntu | Development and execution environment |
| VirtualBox | Running Ubuntu in a virtual machine |

## Features

- Calculator REST API supporting addition, subtraction, multiplication, and division.
- HTTP endpoints for performing calculations.
- Input validation and error handling.
- Maven-based application build and test process.
- Docker image creation using a Dockerfile.
- Jenkins pipeline integrated with GitHub.
- Docker Compose configuration for running the application.
- Nginx reverse proxy for accessing the calculator API.
- Health-check endpoint for checking application availability.

## API Endpoints

The API provides the following endpoints:

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/add?a=2&b=3` | Adds two numbers |
| GET | `/api/v1/subtract?a=10&b=4` | Subtracts the second number from the first |
| GET | `/api/v1/multiply?a=6&b=7` | Multiplies two numbers |
| GET | `/api/v1/divide?a=10&b=4` | Divides the first number by the second |
| GET | `/actuator/health` | Checks application health |

### Example Request

```bash
curl "http://localhost:8081/api/v1/add?a=2&b=3"
```

Example response:

```json
{
  "operation": "add",
  "a": 2,
  "b": 3,
  "result": 5
}
```

The exact response fields depend on the application's implementation.

### Error Handling

The API is designed to return HTTP 400 responses for invalid or missing parameters and division by zero.

## Project Structure

```text
calculator-devops/
├── src/
│   └── main/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── Jenkinsfile
├── nginx/
├── .gitignore
├── .dockerignore
└── README.md
```

The contents of the `src/` and `nginx/` directories may include additional files.

## Prerequisites

Install or configure the following tools:

- Java Development Kit (JDK) 21
- Maven 3.8 or later
- Git
- Docker Engine
- Docker Compose
- Jenkins

The project can be developed and tested inside an Ubuntu virtual machine.

## Build the Application

Clone the repository:

```bash
git clone https://github.com/shashidharsai/calculator-devops.git
```

Navigate into the project directory:

```bash
cd calculator-devops
```

Build the application:

```bash
mvn clean package
```

Run the tests:

```bash
mvn test
```

The packaged JAR is expected to be available at:

```text
target/calculator.jar
```

This filename depends on the Maven configuration.

## Run the Application Locally

After building the application, run:

```bash
java -jar target/calculator.jar
```

Keep the application running. Open another terminal and test the API:

```bash
curl "http://localhost:8080/api/v1/add?a=2&b=3"
```

Check application health:

```bash
curl "http://localhost:8080/actuator/health"
```

## Docker

### Build the Docker Image

From the project root directory, run:

```bash
docker build -t calculator-devops:latest .
```

Verify that the image exists:

```bash
docker images
```

### Run the Container

```bash
docker run -d --name calculator -p 8080:8080 calculator-devops:latest
```

Test the application:

```bash
curl "http://localhost:8080/api/v1/add?a=2&b=3"
```

Check the container logs:

```bash
docker logs calculator
```

Stop and remove the container when finished:

```bash
docker stop calculator
docker rm calculator
```

If port 8080 is already in use, stop the existing service or choose another host port.

## Docker Compose and Nginx

Docker Compose is used to manage the application's container configuration. Nginx provides an entry point for API requests and forwards them to the calculator service.

The configured Nginx endpoint is:

```text
http://localhost:8081
```

### Start the Services

From the project root directory, run:

```bash
docker compose up -d --build
```

### Check Service Status

```bash
docker compose ps
```

### Test the API Through Nginx

Addition:

```bash
curl "http://localhost:8081/api/v1/add?a=2&b=3"
```

Expected result: `5`.

Division:

```bash
curl "http://localhost:8081/api/v1/divide?a=10&b=4"
```

Expected result: `2.5`.

Health check:

```bash
curl "http://localhost:8081/actuator/health"
```

### View Logs

```bash
docker compose logs
```

To view logs continuously:

```bash
docker compose logs -f
```

### Stop the Services

```bash
docker compose down
```

## Jenkins CI Pipeline

Jenkins is configured to retrieve the project from GitHub using Pipeline from SCM.

Repository:

```text
https://github.com/shashidharsai/calculator-devops.git
```

Branch:

```text
main
```

Pipeline script:

```text
Jenkinsfile
```

Jenkins dashboard:

```text
http://localhost:8080
```

### Pipeline Stages

The current Jenkins pipeline contains the following stages:

1. **Checkout:** Retrieves the source code from GitHub.
2. **Build and Test:** Runs the Maven build and tests.
3. **Package Application:** Packages the application.
4. **Build Docker Image:** Builds the Docker image.
5. **Verify Docker Image:** Checks that the image was created.

A successful Jenkins build indicates that the configured pipeline stages completed successfully.

## CI Workflow

The implemented workflow is:

```text
Developer
   |
   v
GitHub Repository
   |
   v
Jenkins Pipeline
   |
   v
Checkout Source Code
   |
   v
Maven Build and Tests
   |
   v
Package Java Application
   |
   v
Build Docker Image
   |
   v
Verify Docker Image
```

Docker Compose and Nginx are used separately to run and access the application.

## Verification

The following functionality has been verified during project testing:

- Jenkins pipeline execution completed successfully.
- Docker image creation through the pipeline succeeded.
- Docker Compose started the application.
- API requests through Nginx on port 8081 returned the expected results for addition and division.

Docker Hub publishing and automated deployment through Jenkins are not included in the current pipeline.

## Security Considerations

- Do not commit passwords, access tokens, or other secrets to GitHub.
- Use Jenkins Credentials when adding credentials to a future pipeline.
- Avoid exposing Jenkins or application ports publicly without appropriate security controls.
- Use appropriate permissions for project files and Docker resources.

## Future Improvements

Possible extensions include:

- Publishing Docker images to Docker Hub from Jenkins.
- Automating application deployment through Jenkins.
- Triggering builds automatically when changes are pushed to GitHub.
- Adding more automated API tests.
- Adding monitoring and logging.
- Deploying the application to a cloud environment.

## Repository

GitHub: https://github.com/shashidharsai/calculator-devops

## Conclusion

This project demonstrates the integration of Java, Maven, GitHub, Jenkins, Docker, Docker Compose, and Nginx in a practical DevOps workflow.

It provides hands-on experience with application builds, testing, containerization, CI pipeline automation, and running a multi-container application.
demo 22:22
