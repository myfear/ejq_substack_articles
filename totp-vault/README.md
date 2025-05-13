# TOTP Vault

TOTP Vault is a Quarkus-based application that integrates with HashiCorp Vault to provide Time-based One-Time Password (TOTP) functionality. It allows users to register and validate TOTP codes securely.

## Features

- **TOTP Key Registration**: Generate and register TOTP keys for users.
- **TOTP Code Validation**: Validate TOTP codes for secure access.
- **HashiCorp Vault Integration**: Securely store and manage TOTP secrets using Vault.

## Prerequisites

- Java 17 or higher
- Maven 3.9.9 or higher
- Docker (optional, for containerized builds)

## Running the Application in Development Mode

You can run the application in development mode with live coding enabled:

```shell
./mvnw quarkus:dev
```

Access the Quarkus Dev UI at [http://localhost:8080/q/dev/](http://localhost:8080/q/dev/).

## Packaging and Running the Application

To package the application:

```shell
./mvnw package
```

This will produce the `quarkus-run.jar` in the `target/quarkus-app/` directory. Run the application using:

```shell
java -jar target/quarkus-app/quarkus-run.jar
```

### Building an Über-Jar

To build an _über-jar_:

```shell
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

Run the _über-jar_ using:

```shell
java -jar target/*-runner.jar
```

## Creating a Native Executable

You can create a native executable using:

```shell
./mvnw package -Dnative
```

If GraalVM is not installed, use a containerized build:

```shell
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Run the native executable:

```shell
./target/totp-vault-1.0.0-SNAPSHOT-runner
```

## Running in a Docker Container

### JVM Mode

Build the Docker image:

```shell
docker build -f src/main/docker/Dockerfile.jvm -t totp-vault-jvm .
```

Run the container:

```shell
docker run -i --rm -p 8080:8080 totp-vault-jvm
```

### Native Mode

Build the Docker image:

```shell
docker build -f src/main/docker/Dockerfile.native -t totp-vault-native .
```

Run the container:

```shell
docker run -i --rm -p 8080:8080 totp-vault-native
```

## Endpoints

### TOTP Registration

- **Endpoint**: `/totp/register/{username}`
- **Method**: `GET`
- **Description**: Generates a TOTP key for the given username and returns a QR code for scanning.

### Protected Resource

- **Endpoint**: `/protected`
- **Method**: `GET`
- **Headers**:
  - `X-User`: Username
  - `X-TOTP-Code`: TOTP code
- **Description**: Validates the TOTP code and grants or denies access.

## Configuration

The application uses the following configuration properties in `application.properties`:

```properties
quarkus.vault.devservices.enabled=true
quarkus.vault.authentication.userpass.username=<vault-username>
quarkus.vault.authentication.userpass.password=<vault-password>
```

## Related Guides

- [Quarkus REST Guide](https://quarkus.io/guides/rest)
- [Quarkus Vault Guide](https://quarkiverse.github.io/quarkiverse-docs/quarkus-vault/dev/index.html)
- [Quarkus Barcode Guide](https://docs.quarkiverse.io/quarkus-barcode/dev/index.html)

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
