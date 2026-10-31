# Calculator DevOps Project

A Java Calculator REST API with a complete CI/CD pipeline. A push to GitHub is picked up by Jenkins, which builds and tests the code, builds a Docker image tagged with the build number, pushes it to Docker Hub, deploys it with Docker Compose behind Nginx, and sends an email with the result.

- GitHub: https://github.com/shashidharsai/calculator-devops
- Docker Hub: https://hub.docker.com/r/shashidhark/calculator-api

## Project Overview

The calculator itself is deliberately simple. The purpose of the project is to show how modern DevOps tools work together to take code from a developer's machine to a running, tested and published application with no manual steps.

```text
Developer -> GitHub -> Jenkins -> Docker Hub
                          |
                          +-> Docker Compose (2 replicas + Nginx) -> Email notification
```

## Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | Application development (plain Java, no framework) |
| Maven | Build management and testing |
| Git / GitHub | Version control and source hosting |
| Jenkins | CI/CD pipeline automation |
| Docker | Application containerization (multi-stage image) |
| Docker Hub | Registry that stores the published images |
| Docker Compose | Running the calculator replicas and Nginx together |
| Nginx | Reverse proxy and single entry point |
| Ubuntu on VirtualBox | Environment where Jenkins, Docker and Compose run |

## Features

- REST API for addition, subtraction, multiplication and division, returning JSON.
- Input validation: HTTP 400 for missing or invalid parameters and for division by zero.
- Health-check endpoint at `/actuator/health`.
- 17 automated tests (8 unit tests and 9 integration tests).
- Multi-stage Dockerfile with dependency caching; the container runs as a non-root user (`app`).
- Docker Compose stack with 2 calculator replicas, health checks, restart policy and a private network.
- Jenkins pipeline defined in the `Jenkinsfile`, triggered automatically by Poll SCM.
- Images tagged with the Jenkins build number and published to Docker Hub.
- Automatic deployment with a health check, and email notification on success or failure.

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/add?a=2&b=3` | Adds two numbers |
| GET | `/api/v1/subtract?a=10&b=4` | Subtracts the second number from the first |
| GET | `/api/v1/multiply?a=6&b=7` | Multiplies two numbers |
| GET | `/api/v1/divide?a=10&b=4` | Divides the first number by the second |
| GET | `/actuator/health` | Checks application health |

Example:

```bash
curl "http://localhost:8081/api/v1/multiply?a=6&b=7"
```

```json
{"operation":"multiply","a":6,"b":7,"result":42}
```

## Project Structure

```text
calculator-devops/
├── src/                 Java source code and tests
├── nginx/               Nginx configuration
├── pom.xml              Maven build configuration
├── Dockerfile           Multi-stage image definition
├── docker-compose.yml   Calculator replicas + Nginx
├── Jenkinsfile          CI/CD pipeline definition
├── .gitignore
├── .dockerignore
└── README.md
```

## Prerequisites

- JDK 21 and Maven 3.8 or later
- Git
- Docker Engine and Docker Compose
- Jenkins (with the Mailer plugin)

Everything (Jenkins, Docker and Compose) runs inside one Ubuntu virtual machine.

## Build and Test Locally

```bash
git clone https://github.com/shashidharsai/calculator-devops.git
cd calculator-devops
mvn clean package
mvn test
```

The packaged JAR is `target/calculator.jar`.

## Run with Docker

```bash
docker build -t calculator-devops:latest .
docker run -d --name calc-test -p 9090:8080 calculator-devops:latest
curl "http://localhost:9090/api/v1/add?a=2&b=3"
docker stop calc-test && docker rm calc-test
```

Port 8080 on the host is used by Jenkins, so the container is published on port 9090.

## Run with Docker Compose and Nginx

```bash
docker compose up -d --build     # build and start the services
docker compose ps                # check that they are running
curl "http://localhost:8081/api/v1/add?a=2&b=3"
curl "http://localhost:8081/actuator/health"
docker compose logs -f           # follow the logs
docker compose down              # stop and remove the containers
```

The stack contains:

- **calculator**: 2 replicas of the API, image `${DOCKERHUB_USER}/calculator-api:${IMAGE_TAG}`, with a health check, a restart policy and a memory limit.
- **nginx** (`nginx:1.27-alpine`): the only published port (8081); it starts after the calculator is healthy and forwards requests to both replicas.

Both services share the private network `calc-net`. To run a specific published image:

```bash
DOCKERHUB_USER=shashidhark IMAGE_TAG=<build-number> docker compose up -d
```

## Jenkins CI/CD Pipeline

Jenkins uses **Pipeline from SCM**: it reads the `Jenkinsfile` from the `main` branch of the GitHub repository. A `pollSCM` trigger checks GitHub about every 2 minutes, so a new commit starts a build automatically (a webhook is not possible because Jenkins runs inside a VirtualBox VM that GitHub cannot reach).

### Pipeline Stages

| # | Stage | What it does |
|---|---|---|
| 1 | Checkout | Retrieves the latest source code from GitHub |
| 2 | Build | `mvn -B clean package -DskipTests` creates the JAR |
| 3 | Test | `mvn -B test` runs the 17 tests and publishes the JUnit reports; a failure stops the pipeline |
| 4 | Docker Build | Builds `shashidhark/calculator-api:<build number>` and also tags it `latest` |
| 5 | Push to Docker Hub | Logs in with the Jenkins credential `dockerhub-creds` and pushes both tags |
| 6 | Deploy | `docker compose up -d`, then polls `/actuator/health` through Nginx and fails if it is not UP in 60 seconds |
| Post | Email | Sends an email on success and on failure |

The deploy step can be re-run safely: `docker compose up -d` only recreates containers whose image or configuration changed.

### One-time Jenkins Setup

1. Add a credential in Manage Jenkins, Credentials: kind *Username with password*, username `shashidhark`, password a Docker Hub access token (Read and Write), ID `dockerhub-creds`.
2. Configure email in Manage Jenkins, System: set the System Admin e-mail address, then under E-mail Notification use `smtp.gmail.com`, SMTP authentication with a Gmail app password, SSL, port 465, and send a test e-mail.
3. Make sure the `jenkins` user can run Docker commands (member of the `docker` group).
4. Create a Pipeline job that uses *Pipeline script from SCM* with this repository and the `main` branch, then run it once so the Poll SCM trigger is registered.

## CI/CD Workflow

```text
Developer pushes to GitHub
        |
        v
Jenkins detects the commit (Poll SCM)
        |
        v
Checkout -> Build -> Test -> Docker Build -> Push to Docker Hub -> Deploy
        |
        v
Email: SUCCESS or FAILED
```

## Verification

The following has been verified:

- All 17 Maven tests pass, locally and in Jenkins.
- The container runs as the non-root user `app`.
- Docker Compose runs two healthy calculator replicas plus Nginx on port 8081.
- Jenkins completes all stages, pushes build-numbered tags to Docker Hub, deploys, and sends a build email.
- Pushing a change to GitHub starts a new build automatically.

## Security Considerations

- No passwords or tokens are stored in GitHub. The Docker Hub token and the SMTP app password exist only in Jenkins.
- The Jenkinsfile references the credential by ID and passes the password to `docker login` through stdin.
- Do not expose Jenkins or the application ports to the public internet without protection.
- Keep Docker, Jenkins and Java updated.

## Future Improvements

- Trigger builds with a GitHub webhook instead of polling.
- Add more automated API tests.
- Add monitoring and logging.
- Deploy to a cloud environment.

## Conclusion

The project delivers a complete CI/CD workflow: code pushed to GitHub is automatically built, tested, packaged into a build-numbered Docker image, published to Docker Hub, deployed with Docker Compose behind Nginx, health-checked, and reported by email.
