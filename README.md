# E-Learning Platform (Monorepo)

A Udemy/Coursera-style e-learning platform built with Spring Boot microservices and Angular.

## Repo Layout

```
elearning-platform/
├── backend/
│   ├── auth-service/        ← DONE (this milestone)
│   ├── user-service/        ← next
│   ├── course-service/      ← next
│   ├── enrollment-service/  ← later
│   ├── quiz-service/        ← later
│   ├── review-service/      ← later
│   ├── certificate-service/ ← later
│   ├── notification-service/← later
│   ├── analytics-service/   ← later
│   ├── api-gateway/         ← later
│   ├── config-server/       ← later
│   └── discovery-server/    ← later (Eureka)
├── frontend/                 ← Angular app (later)
├── docs/                     ← architecture notes, ERDs, API contracts
└── docker-compose.yml        ← grows as each service is added
```

## Build order (recommended)

1. **auth-service** ✅ — registration, login, JWT access + refresh tokens, roles, password reset
2. **discovery-server** + **config-server** + **api-gateway** — wire services together
3. **course-service** — courses, sections, lessons, categories
4. **enrollment-service** — enrollments, progress tracking
5. **quiz-service**, **review-service**, **certificate-service**
6. **notification-service** (RabbitMQ consumer)
7. **analytics-service**
8. **frontend** (Angular) — starts consuming auth-service once it's stable

Each service is independently buildable/runnable — you don't need the whole platform running to work on one service.

## Tech baseline

- Java 21, Spring Boot 3.3.x
- Spring Security 6 + JWT (jjwt)
- Spring Data JPA + MySQL 8 + Flyway migrations
- MapStruct for entity↔DTO mapping
- springdoc-openapi (Swagger UI) for API docs
- Docker / Docker Compose for local orchestration

See `backend/auth-service/README.md` for how to run the first service.
