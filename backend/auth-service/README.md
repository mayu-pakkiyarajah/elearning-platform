# Auth Service

Handles registration, login, JWT issuance/refresh, logout, and password reset for the E-Learning Platform.

## Stack

- Java 21, Spring Boot 3.3
- Spring Security 6 (JWT, BCrypt)
- Spring Data JPA + MySQL 8, Flyway migrations
- MapStruct, Lombok
- springdoc-openapi (Swagger UI)

## Run locally (with Docker Compose — easiest)

From the repo root:

```bash
docker compose up --build auth-mysql auth-service
```

- API base URL: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`

## Run locally (bare Maven, MySQL running separately)

```bash
cp .env.example .env      # edit as needed
export $(cat .env | xargs)
mvn spring-boot:run
```

Or point at a running MySQL instance by exporting `DB_HOST`, `DB_USER`, `DB_PASSWORD`, etc. before `mvn spring-boot:run`.

## Run tests

```bash
mvn test
```

Tests use an in-memory H2 database (`application-test.yml`) — no MySQL required.

## API Reference

| Method | Endpoint                        | Auth required | Description                              |
|--------|----------------------------------|:--------------:|-------------------------------------------|
| POST   | `/api/v1/auth/register`          | No             | Register a STUDENT or INSTRUCTOR account  |
| POST   | `/api/v1/auth/login`             | No             | Log in → `{ accessToken, refreshToken }`  |
| POST   | `/api/v1/auth/refresh`           | No             | Exchange refresh token for a new pair     |
| POST   | `/api/v1/auth/logout`            | No             | Revoke a refresh token                    |
| POST   | `/api/v1/auth/forgot-password`   | No             | Request a password reset token            |
| POST   | `/api/v1/auth/reset-password`    | No             | Reset password using a valid token        |
| GET    | `/api/v1/users/me`               | Yes            | Current authenticated user's profile      |

### Example: register

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ada",
    "lastName": "Lovelace",
    "email": "ada@example.com",
    "password": "StrongPass1",
    "requestedRole": "STUDENT"
  }'
```

### Example: login → use the access token

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ada@example.com","password":"StrongPass1"}' | jq -r .accessToken)

curl http://localhost:8081/api/v1/users/me -H "Authorization: Bearer $TOKEN"
```

## Design notes for the rest of the platform

- **Access tokens are stateless JWTs** (15 min default) carrying `userId`, `roles`, `email` as claims — any
  downstream microservice can verify them locally with the shared `JWT_SECRET` without calling back to
  auth-service. This is what makes it safe to put an API Gateway in front of everything later: the gateway
  (or each service) just validates the signature.
- **Refresh tokens are opaque, DB-backed, single-use (rotated on each `/refresh` call)** — this lets us revoke
  a user's sessions server-side (e.g. on password reset), which a pure-JWT refresh token can't do without a
  blocklist.
- **Instructor accounts start with `instructorApproved = false`.** The Admin/User service (built next) will
  expose an endpoint to flip that flag — course-service should check it before letting someone publish.
- **Password reset currently logs the token instead of emailing it** — wire this to `notification-service`
  via RabbitMQ once that service exists (`PasswordResetRequested` event).
- The shared `JWT_SECRET` (and eventually a shared `roles` contract) is what every other service will trust —
  keep it in the config-server once that's built, not hardcoded per service.

## Next steps in the build order

1. `discovery-server` (Eureka) + `config-server` + `api-gateway`
2. `user-service` (or fold user-profile concerns into this service — your call) with instructor approval, profile fields
3. `course-service`
