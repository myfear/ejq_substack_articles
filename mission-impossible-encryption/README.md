# mission-impossible-encryption

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.


## Remaining Optimizations

### High-Priority Security
- Add Jakarta Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@Email`) to all DTOs
- Implement authentication/authorization (JWT/OAuth2/API keys)
- Add input validation in `AgentResource.enroll()` (null checks, email format, duplicate code names)
- Add null checks in `SpyMessageResource.encrypt()` for request and fields
- Implement rate limiting to prevent DoS/brute force
- Add error handling with custom exceptions and `@ExceptionMapper` (avoid leaking sensitive info)

### Missing Features
- Add agent management endpoints: `GET /agent`, `GET /agent/{codeName}`, `PUT /agent/{codeName}/compromise`, `DELETE /agent/{codeName}`
- Add persistence layer (database or file-based) to replace in-memory storage
- Add OpenAPI/Swagger documentation (`quarkus-smallrye-openapi`)
- Add health checks (`quarkus-smallrye-health`)
- Add metrics/observability (`quarkus-micrometer-registry-prometheus`)
- Implement audit logging for security events

### Code Quality
- Fix test suite: replace placeholder test with real encryption/decryption/enrollment tests
- Convert DTOs to use private fields with getters/setters (or Java records)
- Add proper configuration in `application.properties` (logging, server settings, CORS)

### Performance
- Make key generation async (`@Async` or reactive `Uni`)
- Cache parsed `PGPPublicKey` objects to avoid re-parsing
- Add message size limits to prevent DoS

### Documentation
- Update README with API documentation, usage examples, and architecture overview
