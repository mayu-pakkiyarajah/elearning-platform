# E-Learning Platform (Monorepo)

A Udemy/Coursera-style e-learning platform built with Spring Boot microservices and Angular.

## Repo Layout

```
elearning-platform/
├── backend/
│   ├── auth-service/        ← Done
│   ├── user-service/        ← Done
│   ├── course-service/      ← Done
│   ├── enrollment-service/  ← Done
│   ├── quiz-service/        ← Done
│   ├── review-service/      ← later
│   ├── certificate-service/ ← Backend done, frontend later
│   ├── notification-service/← later
│   ├── analytics-service/   ← later
│   ├── api-gateway/         ← later
│   ├── config-server/       ← later
│   └── discovery-server/    ← later (Eureka)
├── frontend/                 ← Angular app (Updating Parellaly with backend)
├── docs/                     ← architecture notes, ERDs, API contracts
└── docker-compose.yml        ← grows as each service is added
```

## Build order (recommended)

1. **auth-service** ✅ — registration, login, JWT access + refresh tokens, roles, password reset
2. **frontend shell** ✅ — Angular login/register/forgot-password against auth-service, protected dashboard placeholder
3. **course-service** ✅ — courses, sections, lessons, categories, JWT-verify-only security
4. **frontend course features** ✅ — public catalog browse/detail, instructor course + curriculum management
5. **enrollment-service** ✅ — enrollment, lesson progress, course completion; calls course-service via RestClient
6. **frontend enrollment features** ✅ — enroll button, lesson viewer with progress, dashboard shows enrollments
7. **quiz-service** ✅ — quizzes, questions, choices, submissions, scoring; calls course-service + enrollment-service
8. **discovery-server** + **config-server** + **api-gateway** — wire services together
9. **review-service**, **certificate-service**
10. **notification-service** (RabbitMQ consumer)
11. **analytics-service**

Each service is independently buildable/runnable — you don't need the whole platform running to work on one service.

## Tech baseline

- Java 21, Spring Boot 3.3.x
- Spring Security 6 + JWT (jjwt)
- Spring Data JPA + MySQL 8 + Flyway migrations
- MapStruct for entity↔DTO mapping
- springdoc-openapi (Swagger UI) for API docs
- Docker / Docker Compose for local orchestration

See `backend/auth-service/README.md` for how to run the first service.
