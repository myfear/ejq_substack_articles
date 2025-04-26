# Security JPA Quickstart

A Quarkus-based Java application demonstrating security features with JPA-based authentication and PostgreSQL database integration.

## Project Structure

- `src/main/java/` - Contains Java source code
- `src/main/resources/` - Contains configuration files and database migrations
- `pom.xml` - Maven project configuration
- `mvnw` and `mvnw.cmd` - Maven wrapper scripts for Unix and Windows respectively

## Dependencies

The project uses:
- Quarkus 3.21.3
- Hibernate ORM with Panache for database operations
- Security JPA for authentication
- PostgreSQL JDBC driver
- REST for API endpoints

## Prerequisites

- Java 17 or later
- Maven (or use the provided Maven wrapper)
- PostgreSQL database

## Getting Started

1. **Development Mode**
   ```bash
   ./mvnw quarkus:dev
   ```
   This starts the application in development mode with live reloading.

2. **Build and Run**
   ```bash
   ./mvnw package
   java -jar target/quarkus-app/quarkus-run.jar
   ```

3. **Build Native Executable**
   ```bash
   ./mvnw package -Dnative
   ```
   Or using container build:
   ```bash
   ./mvnw package -Dnative -Dquarkus.native.container-build=true
   ```

## Features

- JPA-based user authentication
- PostgreSQL database integration
- REST API endpoints
- Hibernate ORM with Panache for simplified database operations
- Development mode with hot reload
- Native executable support

## Development

- The application runs on `http://localhost:8080`
- Development UI is available at `http://localhost:8080/q/dev/`
- Database configuration is in `src/main/resources/application.properties`

## Testing

Run tests using:
```bash
./mvnw test
```
