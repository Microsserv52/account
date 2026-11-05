# Account

Spring Boot account service for the Microsserv52 platform. It owns account
registration, profile data, password hashes, and roles. Auth reads credentials
through the internal service endpoint; clients never receive password hashes.

## Run locally

```bash
./mvnw test
./mvnw verify
./mvnw spring-boot:run
```

The service uses PostgreSQL in deployment and H2 for tests. Configure
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
`SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, and `INTERNAL_SERVICE_SECRET`
through environment variables.

## GitHub Actions secrets

Add these under **Settings → Secrets and variables → Actions** in
`Microsserv52/account`:

- `SPRING_DATASOURCE_URL`: JDBC URL for PostgreSQL (RDS in AWS).
- `SPRING_DATASOURCE_USERNAME`: database username.
- `SPRING_DATASOURCE_PASSWORD`: database password.
- `JWT_SECRET`: shared JWT HMAC secret used by Gateway and services.
- `INTERNAL_SERVICE_SECRET`: secret required by Auth for credential lookup.
