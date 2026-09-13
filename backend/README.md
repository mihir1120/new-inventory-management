# Ashok Vatika API

Spring Boot backend for the Android SaaS app.

## Why This Backend Exists

The Android app should not connect directly to PostgreSQL in production. The app should call this API over HTTPS, and this API should be the only service that owns database credentials, authorization rules, tenant isolation, validation, audit logging, billing hooks, and backups.

## Production Direction

- Android app: Kotlin client
- Backend: Spring Boot
- Database: PostgreSQL
- Migrations: Flyway
- Auth: JWT resource server
- Tenant model: shared database with `tenant_id` on every business table
- Storage: S3-compatible object storage for uploaded images or documents

## Local Start

Start PostgreSQL:

```bash
docker compose up -d
```

Run the API:

```bash
mvn spring-boot:run
```

Health check:

```text
GET http://localhost:8080/actuator/health
```

## Android API Shape

Every protected request should include:

```text
Authorization: Bearer <jwt>
X-Tenant-Id: 00000000-0000-0000-0000-000000000001
```

Example endpoints:

```text
GET  /api/v1/products
POST /api/v1/products
```

## Important Next Step

`X-Tenant-Id` is acceptable for early local development, but production should derive tenant access from the authenticated JWT claims or a server-side user membership lookup. Do not let users freely choose a tenant header in the final production app.
