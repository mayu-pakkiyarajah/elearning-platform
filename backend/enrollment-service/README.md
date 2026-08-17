# Enrollment Service

Student enrollment, lesson-by-lesson progress tracking, and course completion. Like
course-service, this service only *verifies* JWTs (never issues them) and calls
course-service directly over HTTP to validate courses and get lesson counts — there's
no shared database between services.

## Stack

Same baseline as auth-service/course-service: Java 21, Spring Boot 3.3, Spring
Security 6, Spring Data JPA + MySQL 8 + Flyway, MapStruct, springdoc-openapi. Talks to
course-service via Spring's `RestClient` (no Feign/Eureka yet — see Design notes).

## Run locally

```bash
docker compose up --build auth-mysql auth-service course-mysql course-service enrollment-mysql enrollment-service
```

course-service must be reachable at `COURSE_SERVICE_URL` (defaults to
`http://localhost:8082`, or `http://course-service:8082` inside docker-compose) —
enrolling calls it synchronously to validate the course and snapshot its lesson count.

- API base URL: `http://localhost:8083`
- Swagger UI: `http://localhost:8083/swagger-ui.html`

**Important:** `JWT_SECRET` must match auth-service's exactly, same as course-service.

## Run tests

```bash
mvn test
```

Tests mock `CourseServiceClient` directly (`@MockBean`) rather than standing up a real
course-service — enrollment-service's own logic is what's under test, not the network
hop.

## API Reference

All endpoints require a valid JWT.

| Method | Endpoint                                                    | Role                    | Description                                    |
|--------|---------------------------------------------------------------|--------------------------|--------------------------------------------------|
| POST   | `/api/v1/enrollments`                                       | STUDENT                 | Enroll in a course (idempotent)                |
| GET    | `/api/v1/enrollments/mine`                                  | any authenticated       | List the current student's enrollments         |
| GET    | `/api/v1/enrollments/mine/{courseId}`                       | any authenticated       | Enrollment + which lessons are complete         |
| POST   | `/api/v1/enrollments/mine/{courseId}/lessons/{lessonId}/complete`   | any authenticated | Mark a lesson complete                    |
| DELETE | `/api/v1/enrollments/mine/{courseId}/lessons/{lessonId}/complete`   | any authenticated | Unmark a lesson                           |
| GET    | `/api/v1/enrollments/course/{courseId}`                     | Owning INSTRUCTOR/ADMIN | Roster: who's enrolled, their progress          |

### Example: full flow

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"student@example.com","password":"StrongPass1"}' | jq -r .accessToken)

# Enroll (courseId comes from course-service, e.g. from browsing /api/v1/courses)
curl -X POST http://localhost:8083/api/v1/enrollments \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"courseId": 1}'

# Mark lesson 5 complete
curl -X POST http://localhost:8083/api/v1/enrollments/mine/1/lessons/5/complete \
  -H "Authorization: Bearer $TOKEN"

# Check progress
curl http://localhost:8083/api/v1/enrollments/mine/1 -H "Authorization: Bearer $TOKEN"
```

## Design notes

- **Enrolling calls course-service synchronously** (`CourseServiceClient` →
  `GET /api/v1/courses/id/{id}` on course-service) to confirm the course exists, is
  `PUBLISHED`, and to snapshot its lesson count. If course-service is down, enrolling
  fails with a `503` (`UpstreamServiceException`) rather than silently creating a
  broken enrollment.
- **Course title and lesson count are snapshotted at enrollment time**, not fetched
  live on every read — this service can render "My Courses" without calling
  course-service on every page load. Trade-off: if an instructor adds lessons after a
  student enrolls, that student's progress denominator is stale until they interact
  with the course again in a way that re-syncs it (not currently implemented — a
  `PUT /enrollments/mine/{id}/resync` endpoint would be the natural fix later).
- **A lesson is "complete" by row existence**, not a boolean flag — see
  `LessonProgress`. Marking incomplete just deletes the row.
- **Completing the last lesson auto-flips the enrollment to `COMPLETED`** — see
  `updateCompletionStatus`. There's a TODO there for publishing an
  `EnrollmentCompleted` event once notification-service/certificate-service exist.
- **No service discovery yet.** `CourseServiceClient` hits a hardcoded
  `COURSE_SERVICE_URL`. Once `discovery-server` (Eureka) exists, this is the piece
  that becomes a load-balanced client resolving `course-service` by name instead.
- **The instructor roster (`GET /enrollments/course/{courseId}`) only returns student
  ids**, not names/emails — those live in auth-service, which doesn't yet expose a
  batch "get users by id" endpoint. Wiring that up is a natural next step alongside
  building `user-service`/admin features.

## Next steps

- `certificate-service` can hook into `EnrollmentStatus.COMPLETED` to generate a
  certificate once that event exists
- `analytics-service` will want `countByCourseId` (already on the repository) for
  "popular courses" / enrollment-count metrics
