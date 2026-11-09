# Account

**Application stack**

[![Java](https://img.shields.io/badge/Java-ED8B00?style=flat)](https://dev.java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Bean Validation](https://img.shields.io/badge/Bean_Validation-6DB33F?style=flat)](https://jakarta.ee/specifications/bean-validation/)
[![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=flat)](https://projectlombok.org/)
[![Spring MVC](https://img.shields.io/badge/Spring_MVC-6DB33F?style=flat&logo=spring&logoColor=white)](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![OAuth 2.0](https://img.shields.io/badge/OAuth_2.0-3C6E71?style=flat)](https://oauth.net/2/)
[![JWT](https://img.shields.io/badge/JWT-000000?style=flat&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Spring Actuator](https://img.shields.io/badge/Spring_Actuator-6DB33F?style=flat&logo=springboot&logoColor=white)](https://docs.spring.io/spring-boot/reference/actuator/index.html)
[![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-data-jpa)
[![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=flat&logo=hibernate&logoColor=white)](https://hibernate.org/orm/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![H2](https://img.shields.io/badge/H2-09476B?style=flat)](https://www.h2database.com/)

**Build, checks and delivery**

[![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![JUnit](https://img.shields.io/badge/JUnit-25A162?style=flat&logo=junit5&logoColor=white)](https://junit.org/)
[![Git](https://img.shields.io/badge/Git-F05032?style=flat&logo=git&logoColor=white)](https://git-scm.com/)
[![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat&logo=githubactions&logoColor=white)](https://github.com/features/actions)
[![CodeQL](https://img.shields.io/badge/CodeQL-181717?style=flat&logo=github&logoColor=white)](https://codeql.github.com/)
[![JaCoCo](https://img.shields.io/badge/JaCoCo-D24939?style=flat)](https://www.jacoco.org/jacoco/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![GHCR](https://img.shields.io/badge/GHCR-181717?style=flat&logo=github&logoColor=white)](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)

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

## Browser-facing authorization

`POST /accounts` registers an enabled `ROLE_USER` account. The request cannot
assign administrator roles. `GET /accounts/me` uses the validated JWT subject
to return only that caller's profile, without a password hash.

Listing accounts, reading another account, and deleting accounts require
`ROLE_ADMIN`. Deleting your own account or any administrator account is rejected
with 409; missing accounts return 404. The initial admin interface deliberately
offers account search and read-only details only. Auth's internal credential
lookup retains its service-secret protection and is blocked at the public Gateway.

There is no public role-promotion endpoint or automatic administrator seed.
Administrator setup is an authorized operator action in the owning environment.
Authorization tests run with `./mvnw verify`. Local workstation evidence uses
`-Djava.version=24` because JDK 24 is installed; the normal build target stays 25.

## Environment variables

Local Compose reads these from the root platform `.env`. In GitHub Actions,
store secret values under **Settings → Secrets and variables → Actions** in
`Microsserv52/account`. Kubernetes values belong in `platform-secrets`.

| Variable                     | Purpose                    |
| ---------------------------- | -------------------------- |
| `SPRING_DATASOURCE_URL`      | PostgreSQL JDBC URL        |
| `SPRING_DATASOURCE_USERNAME` | Database username          |
| `SPRING_DATASOURCE_PASSWORD` | Database password          |
| `JWT_SECRET`                 | Shared JWT HMAC secret     |
| `INTERNAL_SERVICE_SECRET`    | Auth-to-Account credential |

## GitHub Actions secrets

Add these under **Settings → Secrets and variables → Actions** in
`Microsserv52/account`:

- `SPRING_DATASOURCE_URL`: JDBC URL for PostgreSQL (RDS in AWS).
- `SPRING_DATASOURCE_USERNAME`: database username.
- `SPRING_DATASOURCE_PASSWORD`: database password.
- `JWT_SECRET`: shared JWT HMAC secret used by Gateway and services.
- `INTERNAL_SERVICE_SECRET`: secret required by Auth for credential lookup.
