# Quarkus OpenTelemetry Adventure

Build real distributed tracing with Quarkus microservices! This project features two services—a Quest Service and a Forge Service—wired together with OpenTelemetry and visualized in Jaeger. Trace requests as they flow across service boundaries, with custom spans and events for deep observability.

## Prerequisites

- JDK 17+
- Maven 3.9.x
- Podman or Docker
- cURL or Postman

## Quick Start

1. **Clone this repo and start Jaeger:**
   ```shell
   podman-compose up
   ```
2. **In two another terminals, run the services:**
   ```shell
   cd forge-service && ./mvnw quarkus:dev
   cd quest-service && ./mvnw quarkus:dev
   ```
3. **Make a request to the Quest Service:**
   ```shell
   curl http://localhost:8080/quests/start
   ```
