# Course Service

Courses, sections, lessons, categories, and course files. This service never issues
JWTs — it only verifies the ones auth-service issues, using the same shared secret.
There's no local user table here; the JWT's `userId` claim is the only thing tying a
course back to its instructor.

## Stack

Same as auth-service: Java 21, Spring Boot 3.3, Spring Security 6, Spring Data JPA +
MySQL 8 + Flyway, MapStruct, springdoc-openapi.

## Run locally

```bash
docker compose up --build course-mysql course-service
```

- API base URL: `http://localhost:8082`
- Swagger UI: `http://localhost:8082/swagger-ui.html`

**Important:** `JWT_SECRET` here must exactly match auth-service's `JWT_SECRET` — a
token minted by auth-service has to verify successfully here too. In `docker-compose.yml`
both services currently point at the same placeholder value; change both together.

## Run tests

```bash
mvn test
```

Tests run against H2 and use `TestJwtFactory` to mint tokens signed with the same
test secret course-service itself trusts — so tests don't need auth-service running.

## API Reference

| Method | Endpoint                                  | Auth                        | Description                                  |
|--------|--------------------------------------------|------------------------------|-----------------------------------------------|
| GET    | `/api/v1/categories`                       | No                           | List all categories                           |
| POST   | `/api/v1/categories`                       | ADMIN                        | Create a category                             |
| PUT    | `/api/v1/categories/{id}`                  | ADMIN                        | Update a category                             |
| DELETE | `/api/v1/categories/{id}`                  | ADMIN                        | Delete a category                             |
| GET    | `/api/v1/courses`                          | No                           | Browse published courses (filter/search/page) |
| GET    | `/api/v1/courses/{slug}`                   | No                           | Full course detail (sections + lessons)       |
| GET    | `/api/v1/courses/mine`                     | Yes                          | Instructor's own courses, incl. drafts        |
| POST   | `/api/v1/courses`                          | INSTRUCTOR                   | Create a course (starts as DRAFT)             |
| PUT    | `/api/v1/courses/{id}`                     | Owning INSTRUCTOR or ADMIN   | Update a course                               |
| DELETE | `/api/v1/courses/{id}`                     | Owning INSTRUCTOR or ADMIN   | Delete a course                               |
| PATCH  | `/api/v1/courses/{id}/publish`             | Owning INSTRUCTOR or ADMIN   | Publish → visible in public catalog           |
| PATCH  | `/api/v1/courses/{id}/unpublish`           | Owning INSTRUCTOR or ADMIN   | Revert to DRAFT                               |
| POST   | `/api/v1/courses/{courseId}/sections`      | Owning INSTRUCTOR or ADMIN   | Add a section                                 |
| PUT    | `/api/v1/sections/{id}`                    | Owning INSTRUCTOR or ADMIN   | Update a section                              |
| DELETE | `/api/v1/sections/{id}`                    | Owning INSTRUCTOR or ADMIN   | Delete a section (and its lessons)            |
| POST   | `/api/v1/sections/{sectionId}/lessons`     | Owning INSTRUCTOR or ADMIN   | Add a lesson                                  |
| PUT    | `/api/v1/lessons/{id}`                     | Owning INSTRUCTOR or ADMIN   | Update a lesson                               |
| DELETE | `/api/v1/lessons/{id}`                     | Owning INSTRUCTOR or ADMIN   | Delete a lesson                               |

### Example: full flow

```bash
# 1. Log in via auth-service to get a token (see backend/auth-service/README.md)
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"instructor@example.com","password":"StrongPass1"}' | jq -r .accessToken)

# 2. Create a course
COURSE=$(curl -s -X POST http://localhost:8082/api/v1/courses \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"Intro to Spring Boot","level":"BEGINNER","language":"English","price":29.99}')
COURSE_ID=$(echo $COURSE | jq -r .id)

# 3. Add a section
SECTION=$(curl -s -X POST http://localhost:8082/api/v1/courses/$COURSE_ID/sections \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"Getting Started","position":1}')
SECTION_ID=$(echo $SECTION | jq -r .id)

# 4. Add a lesson
curl -X POST http://localhost:8082/api/v1/sections/$SECTION_ID/lessons \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"Welcome","position":1,"contentType":"VIDEO","videoUrl":"https://example.com/v.mp4","isPreview":true}'

# 5. Publish it
curl -X PATCH http://localhost:8082/api/v1/courses/$COURSE_ID/publish -H "Authorization: Bearer $TOKEN"

# 6. Anyone can now browse it (no token needed)
curl http://localhost:8082/api/v1/courses
```

## Design notes

- **No JWT issuance here, verify-only.** `JwtAuthenticationFilter` builds an
  `AuthenticatedUser` (userId, email, roles) straight from the token claims — no DB
  lookup, no dependency on auth-service being reachable at request time. This is the
  pattern every future service (quiz, enrollment, review, ...) should follow.
- **Ownership checks live in the service layer**, not just `@PreAuthorize` — role
  alone ("is an INSTRUCTOR") isn't enough, an instructor must also *own* the specific
  course/section/lesson they're editing. See `getOwnedCourseOrThrow` and the
  `assertOwnerOrAdmin` helpers.
- **Slugs are generated once, on create, and never touched on update** — so a
  published course's URL survives a title edit. Collisions get a `-2`, `-3`, ... suffix.
- **Draft courses never appear in `/api/v1/courses` (the public browse endpoint)** —
  only `PUBLISHED` ones do. Instructors see their drafts via `/api/v1/courses/mine`.
- **`instructorApproved` isn't enforced yet** — the JWT doesn't currently carry that
  claim (see the TODO in `CourseServiceImpl.setPublished`). Once auth-service's token
  includes it, gate publishing on it there.

## Next steps

- `enrollment-service` will need to check `courses.status == PUBLISHED` and read
  lesson previews before a student enrolls
- Video/file upload (`course_files` table already exists) — currently only accepts a
  `videoUrl` string; an actual upload endpoint (multipart → S3/local disk) isn't built
