# Sentiment Analysis API

A Quarkus-based REST API for performing sentiment analysis on text using LangChain4j and Ollama.

## Overview

This project provides a sentiment analysis service that leverages LangChain4j and Ollama to analyze the sentiment of text input. It's built using Quarkus, a Kubernetes-native Java framework tailored for GraalVM and HotSpot, crafted from the best of breed Java libraries and standards.

## Features

- REST API endpoints for sentiment analysis
- Integration with LangChain4j and Ollama for natural language processing
- Fast startup time and low memory footprint
- Native compilation support for optimal performance

## Prerequisites

- Java 17 or later
- Maven
- Ollama (for running the language model)

## Running the Application

### Development Mode

To run the application in development mode (with live coding enabled):

```shell script
./mvnw quarkus:dev
```

The application will be available at `http://localhost:8080`.

### Production Mode

To build and run the application in production mode:

```shell script
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Native Executable

To create a native executable:

```shell script
./mvnw package -Dnative
```

Or, to build in a container (if you don't have GraalVM installed):

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Run the native executable with:
```shell script
./target/sentiment-analysis-1.0.0-SNAPSHOT-runner
```

## API Documentation

Once the application is running, you can access:
- Swagger UI at `http://localhost:8080/q/swagger-ui`
- OpenAPI documentation at `http://localhost:8080/q/openapi`

## Development

- The Dev UI is available in dev mode at `http://localhost:8080/q/dev/`
- Live coding is enabled in dev mode
- Tests can be run with `./mvnw test`

## Project Structure

- `src/main/java` - Java source code
- `src/main/resources` - Configuration files and resources
- `src/test` - Test source code
- `pom.xml` - Maven configuration

## Dependencies

- Quarkus REST Jackson - For JSON serialization
- LangChain4j Ollama - For natural language processing
- Quarkus Arc - For dependency injection
- Quarkus REST - For REST endpoints

## License

[Add your license information here]
