# The JAR is built by the CI workflow's own `mvn clean package` step, on the GitHub Actions runner
# — which has working Google Cloud auth (Workload Identity), so it can resolve the private
# com.forgecrux:forge-auth-lib / com.forge.libs:* dependencies from Artifact Registry. This
# Dockerfile used to ALSO carry a `FROM maven:... AS builder` stage running its own `mvn` inside
# Docker; that stage is a fully isolated build container with no gcloud session, so it could never
# authenticate to Artifact Registry. It was also unused (no COPY --from=builder), so buildx pruned
# it and the runtime COPY below failed instead. Just packaging the already-built JAR is correct.
FROM eclipse-temurin:17-jre-alpine

# Install wget for health checks (before switching to non-root user)
RUN apk add --no-cache wget

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the JAR built by the workflow's `mvn clean package` step (must run before this Docker
# build step — see .github/workflows/deploy_prod.yml). .dockerignore keeps the rest of target/ out.
COPY target/fsp-compliance-svc-*.jar app.jar

RUN chown spring:spring app.jar

# Writable log directory in /tmp (works with a read-only root filesystem)
RUN mkdir -p /tmp/logs

USER spring:spring

ENV PORT=8080
EXPOSE ${PORT}

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/compliance-api/actuator/health || exit 1

# Run the application.
#
# MaxRAMPercentage: without it the JVM defaults to 25% of the container, so a 512 MiB instance ran
#   on a ~128 MB heap while the rest sat unused. 70% leaves room for metaspace, code cache, thread
#   stacks and direct buffers, which are not counted in the heap but are counted by Cloud Run.
# ExitOnOutOfMemoryError: an instance that has exhausted its heap cannot serve anything useful, and
#   left alive it lingers behind the load balancer returning 502s. Exiting lets the platform replace it.
#
# Deliberately no HeapDumpOnOutOfMemoryError: /tmp is a tmpfs here, so the dump would be written
# into the very memory that just ran out.
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=70.0 -XX:+ExitOnOutOfMemoryError -jar -Dserver.port=${PORT} app.jar"]
