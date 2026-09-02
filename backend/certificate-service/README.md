# Certificate Service

Generates, stores, and verifies PDF completion certificates. Certificates are
generated once and stored as a BLOB (`pdf_data`) in the database — not regenerated
on every download — so a certificate's exact bytes never change after issuance.

## Stack

Same baseline as the other services: Java 21, Spring Boot 3.3, Spring Security 6,
Spring Data JPA + MySQL 8 + Flyway, Spring `RestClient` for calling auth-service and
enrollment-service. PDF rendering via **Apache PDFBox 2.x**; the QR code embedded in
every certificate is generated with **ZXing**.

## Run locally

```bash
docker compose up --build \
  auth-mysql auth-service \
  course-mysql course-service \
  enrollment-mysql enrollment-service \
  certificate-mysql certificate-service
```

- API base URL: `http://localhost:8085`
- Swagger UI: `http://localhost:8085/swagger-ui.html`

`JWT_SECRET` must match auth-service's exactly.

## Run tests

```bash
mvn test
```

`AuthServiceClient` and `EnrollmentServiceClient` are mocked at the Spring bean level
— the test suite covers the eligibility check (must have `COMPLETED` status),
idempotent generation, PDF magic-byte verification, cross-student access denial, and
public verification (both valid and unknown codes).

## API Reference

| Method | Endpoint                              | Auth              | Description                                   |
|--------|------------------------------------------|--------------------|--------------------------------------------------|
| POST   | `/api/v1/certificates`                | STUDENT            | Generate a certificate for a completed course (idempotent) |
| GET    | `/api/v1/certificates/mine`           | any authenticated  | List your own certificates                    |
| GET    | `/api/v1/certificates/{id}/download`  | Owner or ADMIN     | Download your own certificate PDF             |
| GET    | `/api/v1/certificates/verify/{code}`  | **None — public**  | Verify a certificate by its code              |
| GET    | `/api/v1/certificates/verify/{code}/pdf` | **None — public** | View/download the PDF for a shared link    |

### Example

```bash
STUDENT_TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"student@example.com","password":"StrongPass1"}' | jq -r .accessToken)

# Generate (requires the course to already be COMPLETED in enrollment-service)
CERT=$(curl -s -X POST http://localhost:8085/api/v1/certificates \
  -H "Authorization: Bearer $STUDENT_TOKEN" -H "Content-Type: application/json" \
  -d '{"courseId": 1}')
echo $CERT | jq

CODE=$(echo $CERT | jq -r .verificationCode)

# Anyone can verify it — no login required
curl http://localhost:8085/api/v1/certificates/verify/$CODE

# Anyone with the code can view/download the actual PDF too
curl http://localhost:8085/api/v1/certificates/verify/$CODE/pdf -o certificate.pdf
```

## Design notes

- **A security gap in the original scaffold, fixed during this build:** the internal
  user-lookup endpoint this service depends on
  (`auth-service`'s `GET /api/v1/users/id/{id}`) was initially left fully public
  (`permitAll`), mirroring course-service's public course-lookup endpoint. That's
  fine for course metadata, but names and emails are PII — a public endpoint would
  let anyone enumerate user IDs and harvest real names/emails with no
  authentication at all. It's now locked down to require **any valid JWT** (not
  specifically the user being looked up, since there's no service-to-service auth
  story yet — see below), and `AuthServiceClient` forwards the calling student's own
  Bearer token to satisfy that. Worth knowing if you're reviewing this codebase: this
  is exactly the kind of easy-to-miss cross-service security issue that only shows up
  once you trace a real call path end to end, not by reading either service in
  isolation.
- **Idempotent generation** — calling `POST /certificates` again for a course you
  already have a certificate for just returns the existing one (same pattern as
  enrollment-service's `POST /enrollments`), keyed by a `(student_id, course_id)`
  unique constraint.
- **Verification codes** are 10 characters from a 32-character alphabet that
  deliberately excludes visually ambiguous characters (`0`/`O`, `1`/`I`/`L`) — they
  get printed on a certificate someone might read aloud or retype.
- **The QR code encodes the same public verification URL** a share link would use
  (`{FRONTEND_URL}/certificates/verify/{code}`) — scanning it and clicking the link
  land you on the same page. This was one of the "advanced features" called out in
  the original project brief, implemented here rather than left as a TODO.
- **No service discovery yet** — `AuthServiceClient` and `EnrollmentServiceClient`
  hit hardcoded URLs, same seam as every other service that calls out.

## Next steps

- The frontend needs a `/certificates/verify/:code` public page for the QR code /
  share link to actually land on (currently just an API response)
- A "My Certificates" page in the student dashboard, with a "Generate certificate"
  button that appears once `enrollment.status === 'COMPLETED'`
- A proper service-to-service auth story (API keys or mTLS) would let
  `GET /users/id/{id}` be restricted more precisely than "any authenticated caller"
