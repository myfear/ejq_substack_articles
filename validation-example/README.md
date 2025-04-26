# Validation Example Project

A Quarkus-based Java application demonstrating validation features and REST API development.

## Project Structure

- `src/main/java/` - Contains Java source code
- `src/main/resources/` - Contains configuration files and templates
- `src/main/docker/` - Docker-related files
- `pom.xml` - Maven project configuration
- `mvnw` and `mvnw.cmd` - Maven wrapper scripts for Unix and Windows respectively

## Dependencies

The project uses:
- Quarkus 3.21.4
- REST Jackson for JSON serialization
- Qute Web for template rendering
- Hibernate Validator for validation
- JUnit 5 for testing

## Getting Started

1. **Prerequisites**
   - Java 17 or later
   - Maven (or use the provided Maven wrapper)

2. **Development Mode**
   ```bash
   ./mvnw quarkus:dev
   ```
   This starts the application in development mode with live reloading.

3. **Build and Run**
   ```bash
   ./mvnw package
   java -jar target/quarkus-app/quarkus-run.jar
   ```

4. **Build Native Executable**
   ```bash
   ./mvnw package -Dnative
   ```
   Or using container build:
   ```bash
   ./mvnw package -Dnative -Dquarkus.native.container-build=true
   ```

## Features

- REST API endpoints with JSON serialization
- Template-based web pages using Qute
- Input validation using Hibernate Validator
- Development mode with hot reload
- Native executable support

## Development

- The application runs on `http://localhost:8080`
- Development UI is available at `http://localhost:8080/q/dev/`
- Qute templates are served from `src/main/resources/templates/pub/`

## Testing

Run tests using:
```bash
./mvnw test
```
