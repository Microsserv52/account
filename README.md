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

## Browser-facing authorization

`POST /accounts` registers an enabled `ROLE_USER` account. The request cannot
assign administrator roles. `GET /accounts/me` uses the validated JWT subject
to return only that caller's profile, without a password hash.

Listing accounts, reading another account, and deleting accounts require
`ROLE_ADMIN`. Deleting your own account or any administrator account is rejected
with 409; missing accounts return 404. The initial admin interface deliberately
offers account search and read-only details only. Auth's internal credential
lookup retains its service-secret protection and is blocked at the public Gateway.

There is no public role-promotion endpoint. For **local Compose testing only**, an
opt-in bootstrap can seed or promote a local administrator at startup: set both
`ADMIN_EMAIL` and `ADMIN_PASSWORD` and the service creates that account with
`ROLE_ADMIN,ROLE_USER` (or promotes it if it already exists, preserving its
password and enabled flag). The local platform Compose fallback uses
`admin@microsserv52.com` / `1berto52`; the root `.env.example` leaves both
variables empty, which disables the bootstrap. Setting exactly one fails
startup, and the bootstrap refuses to run when `ENV` or `APP_ENV` is
`prod`/`production` (case-insensitive) or any comma-separated
`SPRING_PROFILES_ACTIVE` token is `prod`/`production` — Account exits instead of
creating a privileged account. Never set these in real environments; promote
real accounts with bound-parameter SQL and record the operator action.
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
| `ADMIN_EMAIL`                | LOCAL-ONLY admin bootstrap email; empty disables |
| `ADMIN_PASSWORD`             | LOCAL-ONLY admin bootstrap password; never set in production |

## GitHub Actions secrets

Add these under **Settings → Secrets and variables → Actions** in
`Microsserv52/account`:

- `SPRING_DATASOURCE_URL`: JDBC URL for PostgreSQL (RDS in AWS).
- `SPRING_DATASOURCE_USERNAME`: database username.
- `SPRING_DATASOURCE_PASSWORD`: database password.
- `JWT_SECRET`: shared JWT HMAC secret used by Gateway and services.
- `INTERNAL_SERVICE_SECRET`: secret required by Auth for credential lookup.

Do **not** add `ADMIN_EMAIL`/`ADMIN_PASSWORD` to Actions secrets or
`platform-secrets`. They exist only for the local Compose developer fallback;
alongside a `prod` marker the service refuses to start rather than seed an
administrator.
