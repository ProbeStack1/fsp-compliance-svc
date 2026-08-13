# probestack-compliance-service-api

Generated Spring Boot application from OpenAPI specification.

## Project Details
- **Group ID**: com.probestack.forgesphere
- **Artifact ID**: probestack-compliance-service-api
- **Version**: 1.0.0
- **Base Package**: com.probestack.forgesphere

## Building the Project
```bash
mvn clean install
```


## Running the Application
```bash
mvn spring-boot:run
```

## API Documentation
Once the application is running, access the Swagger UI at:
- http://localhost:8080/swagger-ui.html

## API Docs (OpenAPI)
- http://localhost:8080/api-docs

## Cloud Run Deployment
This generated project includes GitHub Actions CI/CD for Google Cloud Run.

- Workflow: `.github/workflows/ci-cd.yml`
- Service name: `probestack-compliance-service-api`
- GCP project: `probestack-prod`
- Region: `us-central1`
- Artifact Registry repository: `us-central1-docker.pkg.dev/probestack-prod/probestack-prod-apps`

On every GitHub push, the workflow builds the application, publishes a Docker image, deploys to Cloud Run, and verifies `/actuator/health`.

See `DEPLOYMENT.md` for setup instructions.
