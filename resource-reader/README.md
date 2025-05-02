# Resource Reader Application

A simple REST service built with Quarkus that reads and serves text content from classpath resources.

## Overview

This application provides a REST endpoint that reads and returns the contents of a text file (`my-data.txt`) from the classpath. It's designed as a simple demonstration of resource handling in a Quarkus application.

### Key Features
- REST endpoint at `/resource` that returns text content
- Handles resource loading from the classpath
- Provides proper error handling for missing resources
- Built with Quarkus for cloud-native deployment

## Running the Application

### Development Mode
Run the application in development mode with live coding enabled:
```shell script
./mvnw quarkus:dev
```

The application will be available at `http://localhost:8080/resource`.

### Building and Running
To build the application:
```shell script
./mvnw package
```

Run the packaged application:
```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

### Native Build
Create a native executable:
```shell script
./mvnw package -Dnative
```

Or build in a container:
```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Technical Details

- Built with Quarkus, the Supersonic Subatomic Java Framework
- Uses REST Jackson for JSON serialization
- Implements proper resource handling with error management
- Supports both development and production deployment modes

For more information about Quarkus, visit: <https://quarkus.io/>

## Related Guides

- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
