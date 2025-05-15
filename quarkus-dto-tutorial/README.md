# quarkus-dto-tutorial

This project demonstrates a simple CRUD REST API using Quarkus, DTOs, MapStruct, and Hibernate ORM with Panache.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Features

- REST endpoints for managing blog posts (`/posts`)
- DTO pattern for request/response separation
- MapStruct for automatic mapping between entities and DTOs
- Hibernate ORM with Panache for data persistence
- Validation using Hibernate Validator
- OpenAPI/Swagger UI documentation

## Running the application in dev mode

You can run your application in dev mode with live coding enabled using:

```shell
./mvnw quarkus:dev
```

Visit the Dev UI at [http://localhost:8080/q/dev/](http://localhost:8080/q/dev/).

## Packaging and running the application

To package the application:

```shell
./mvnw package
```

This produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.  
Run it with:

```shell
java -jar target/quarkus-app/quarkus-run.jar
```

To build an _über-jar_:

```shell
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

Run the über-jar with:

```shell
java -jar target/*-runner.jar
```

## Creating a native executable

You can create a native executable using:

```shell
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, build in a container:

```shell
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Run the native executable with:

```shell
./target/quarkus-dto-tutorial-1.0.0-SNAPSHOT-runner
```

## Running with Docker

Dockerfiles are provided in [src/main/docker](src/main/docker):

- JVM mode: `Dockerfile.jvm`
- Legacy JAR: `Dockerfile.legacy-jar`
- Native: `Dockerfile.native`
- Native micro: `Dockerfile.native-micro`

Example (JVM mode):

```shell
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/quarkus-dto-tutorial-jvm .
docker run -i --rm -p 8080:8080 quarkus/quarkus-dto-tutorial-jvm
```

## API Endpoints

- `POST /posts` – Create a new post
- `GET /posts` – List all posts
- `GET /posts/{id}` – Get a post by ID
- `PUT /posts/{id}` – Update a post
- `DELETE /posts/{id}` – Delete a post

OpenAPI docs available at [http://localhost:8080/q/openapi](http://localhost:8080/q/openapi)  
Swagger UI at [http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui)

## Running Tests

To run tests:

```shell
./mvnw test
```

## Related Guides

- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [REST Jackson](https://quarkus.io/guides/rest#json-serialisation)
- [Hibernate Validator](https://quarkus.io/guides/validation)
- [Camel MapStruct](https://camel.apache.org/camel-quarkus/latest/reference/extensions/mapstruct.html)
- [SmallRye OpenAPI](https://quarkus.io/guides/openapi-swaggerui)
- [JDBC Driver - H2](https://quarkus.io/guides/datasource)
