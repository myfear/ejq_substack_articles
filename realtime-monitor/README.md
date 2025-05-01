# Real-time JVM Heap Monitor

A lightweight real-time monitoring application that tracks and displays JVM heap memory usage using WebSockets. Built with Quarkus for high performance and low resource consumption.

## Features

- Real-time monitoring of JVM heap memory usage
- WebSocket-based updates every 3 seconds
- Clean, responsive web interface
- Built with Quarkus for optimal performance
- Supports both JVM and native execution modes

## Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.9.9 or later

### Running in Development Mode

```shell script
./mvnw quarkus:dev
```

The application will be available at:
- Web interface: http://localhost:8080
- Dev UI: http://localhost:8080/q/dev/

### Building and Running

1. Package the application:
```shell script
./mvnw package
```

2. Run the application:
```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

### Building Native Executable

To create a native executable for optimal performance:

```shell script
./mvnw package -Dnative
```

Or, to build in a container (if you don't have GraalVM installed):

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Architecture

The application consists of:
- A WebSocket endpoint (`/monitor/heap`) that broadcasts heap usage data
- A simple web interface that displays the current heap usage
- Scheduled task that collects heap metrics every 3 seconds

## License

This project is licensed under the Apache License 2.0.
