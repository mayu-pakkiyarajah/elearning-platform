# Quiz Service

Quizzes, questions, multiple-choice answers, submissions, and scoring. This is the
first service that calls **two** other services on the request path: course-service
(to check who owns a course) and enrollment-service (to check who's enrolled in one).

## Stack

Same baseline as the other services: Java 21, Spring Boot 3.3, Spring Security 6,
Spring Data JPA + MySQL 8 + Flyway, MapStruct, springdoc-openapi, Spring `RestClient`
for the two upstream calls.

## Run locally

```bash
docker compose up --build \
  auth-mysql auth-service \
  course-mysql course-service \
  enrollment-mysql enrollment-service \
  quiz-mysql quiz-service
```

- API base URL: `http://localhost:8084`
- Swagger UI: `http://localhost:8084/swagger-ui.html`

`JWT_SECRET` must match auth-service's exactly, same as every other service.

## Run tests

```bash
mvn test
```

Both `CourseServiceClient` and `EnrollmentServiceClient` are mocked at the Spring bean
level (`@MockBean`) — quiz-service's own authorization and scoring logic is what's
under test, not the two network hops.

## API Reference

All endpoints require a valid JWT.

### Instructor: building a quiz

| Method | Endpoint                                     | Role                    | Description                          |
|--------|------------------------------------------------|--------------------------|----------------------------------------|
| POST   | `/api/v1/courses/{courseId}/quizzes`          | Owning INSTRUCTOR       | Create a quiz                        |
| GET    | `/api/v1/courses/{courseId}/quizzes`          | Owner/admin or enrolled student | List a course's quizzes       |
| GET    | `/api/v1/quizzes/{id}`                        | Owning INSTRUCTOR/ADMIN | Full detail **with correct answers** |
| PUT    | `/api/v1/quizzes/{id}`                        | Owning INSTRUCTOR/ADMIN | Update title/description/passing score |
| DELETE | `/api/v1/quizzes/{id}`                        | Owning INSTRUCTOR/ADMIN | Delete a quiz                        |
| POST   | `/api/v1/quizzes/{quizId}/questions`          | Owning INSTRUCTOR/ADMIN | Add a question with its choices      |
| PUT    | `/api/v1/questions/{id}`                      | Owning INSTRUCTOR/ADMIN | Replace a question's text/choices    |
| DELETE | `/api/v1/questions/{id}`                      | Owning INSTRUCTOR/ADMIN | Delete a question                    |
| GET    | `/api/v1/quizzes/{id}/submissions`            | Owning INSTRUCTOR/ADMIN | See every student's attempts         |

### Student: taking a quiz

| Method | Endpoint                                | Description                                          |
|--------|-------------------------------------------|--------------------------------------------------------|
| GET    | `/api/v1/quizzes/{id}/take`             | Get the quiz — **requires enrollment**, no correct answers included |
| POST   | `/api/v1/quizzes/{id}/submit`           | Submit answers, scored immediately, returns per-question correctness |
| GET    | `/api/v1/quizzes/{id}/submissions/mine` | Own past attempts on this quiz                        |

### Example: build and take a quiz

```bash
INSTRUCTOR_TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"instructor@example.com","password":"StrongPass1"}' | jq -r .accessToken)

# Create a quiz for course 1
QUIZ=$(curl -s -X POST http://localhost:8084/api/v1/courses/1/quizzes \
  -H "Authorization: Bearer $INSTRUCTOR_TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"Chapter 1 Quiz","passingScorePercent":70}')
QUIZ_ID=$(echo $QUIZ | jq -r .id)

# Add a question
curl -X POST http://localhost:8084/api/v1/quizzes/$QUIZ_ID/questions \
  -H "Authorization: Bearer $INSTRUCTOR_TOKEN" -H "Content-Type: application/json" \
  -d '{
    "text": "What is 2 + 2?", "position": 1, "points": 1,
    "choices": [
      {"text": "3", "position": 1, "correct": false},
      {"text": "4", "position": 2, "correct": true},
      {"text": "5", "position": 3, "correct": false}
    ]
  }'

# Student (must be enrolled) takes it
STUDENT_TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"student@example.com","password":"StrongPass1"}' | jq -r .accessToken)

curl http://localhost:8084/api/v1/quizzes/$QUIZ_ID/take -H "Authorization: Bearer $STUDENT_TOKEN"

# Submit an answer (choiceId from the /take response)
curl -X POST http://localhost:8084/api/v1/quizzes/$QUIZ_ID/submit \
  -H "Authorization: Bearer $STUDENT_TOKEN" -H "Content-Type: application/json" \
  -d '{"answers": [{"questionId": 1, "choiceId": 2}]}'
```

## Design notes

- **Two upstream service calls, two different patterns.** `CourseServiceClient` calls
  course-service's own service account context (no user token needed — it's public
  metadata). `EnrollmentServiceClient` forwards the *caller's own Bearer token*,
  because enrollment-service's `/enrollments/mine/{courseId}` deliberately only
  answers "is the calling user enrolled" — there's no "check enrollment for arbitrary
  student X" endpoint. This is a real limitation worth knowing: a background job
  (e.g. a nightly digest) couldn't use this same client without its own service
  credential, which doesn't exist yet anywhere in this platform.
- **Correct answers never reach a student.** `QuizTakeResponse`/`QuestionTakeResponse`/
  `ChoiceTakeResponse` are a deliberately separate set of DTOs from the
  instructor-facing `QuizDetailResponse`/`QuestionResponse`/`ChoiceResponse` — the
  student-facing mapper method (`toTakeResponse`) has no path to the `correct` field
  at all, so it's not a filter that could accidentally be bypassed, it's a type that
  doesn't have the field.
- **Scoring happens entirely server-side, from the correct-choice IDs stored in the
  DB** — the client never has to be trusted to say what's correct. See
  `QuizServiceImpl.submitQuiz`.
- **A question must have exactly one correct choice** — enforced in the service layer
  (`assertExactlyOneCorrectChoice`), not the DB, since MySQL doesn't have an easy way
  to express "exactly one row per group has this flag set" as a constraint.
- **Updating a question replaces its entire choice list** (clear + rebuild) rather
  than trying to diff and match existing choices by id — simpler, and questions are
  edited rarely enough that this isn't a performance concern.
- **No service discovery yet** — both `CourseServiceClient` and
  `EnrollmentServiceClient` hit hardcoded URLs (`COURSE_SERVICE_URL`,
  `ENROLLMENT_SERVICE_URL`). Same seam as enrollment-service's own course-service
  call: this is what becomes Eureka-resolved once `discovery-server` exists.

## Next steps

- Publish a `QuizPassed` event once notification-service exists (see the TODO in
  `QuizServiceImpl.submitQuiz`) — ties into the "Quiz Passed" notification from the
  original spec and could gate certificate-service eligibility alongside course
  completion
- A proper service-to-service auth story (API keys or mTLS between services) would
  remove the need for `EnrollmentServiceClient` to forward user tokens
