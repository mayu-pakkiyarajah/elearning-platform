# EduPath Frontend

Angular 18 (standalone components) frontend for the E-Learning Platform. Currently wired
up to `auth-service` only — register, login, forgot/reset password, and a protected
dashboard placeholder.

## Stack

- Angular 18, standalone components, signals for reactive state
- Bootstrap 5 (SCSS import, not the JS bundle — no jQuery dependency)
- Reactive Forms with validation matching the backend's rules
- A functional HTTP interceptor for JWT attach + silent refresh-on-401

## Run locally

Make sure `auth-service` is running first (see `../backend/auth-service/README.md`),
then:

```bash
npm install
npm start
```

This runs `ng serve` with `proxy.conf.json`, which forwards `/api/*` requests to
`http://localhost:8081` (auth-service) — so the app can call `/api/v1/auth/login`
etc. without hitting CORS in dev, without hardcoding `localhost:8081` into the app
itself. That matters later: once the API Gateway exists, only the proxy target
changes, not the app code.

Open `http://localhost:4200`.

## Build

```bash
npm run build              # production build → dist/frontend
npm run build -- --configuration development
```

Font inlining is disabled in the production build config (`optimization.fonts: false`)
— Angular's default build tries to fetch and inline Google Fonts at build time, which
is a network dependency you don't want in a Docker build or CI pipeline. The fonts
still load fine at runtime via the `<link>` tag in `index.html`.

## Structure

```
src/app/
├── core/
│   ├── guards/        authGuard, guestGuard, roleGuard
│   ├── interceptors/  jwtInterceptor (attach token, silent refresh on 401)
│   ├── models/        User, Course, Section, Lesson, etc. — mirror backend DTOs exactly
│   └── services/      AuthService, TokenStorageService, CourseService, SectionService,
│                       LessonService, CategoryService
├── shared/
│   └── components/    alert, navbar — reused across features
├── layouts/
│   ├── auth-layout/    split-screen shell for login/register/forgot-password
│   └── main-layout/    navbar + router-outlet for everything else
└── features/
    ├── auth/
    │   ├── login/
    │   ├── register/
    │   └── forgot-password/
    ├── courses/                 public — no login required
    │   ├── course-list/          catalog browse with search/filter/pagination
    │   └── course-detail/        full detail page with curriculum accordion
    ├── instructor/               requires ROLE_INSTRUCTOR (roleGuard)
    │   ├── my-courses/            instructor's own courses, incl. drafts; publish/unpublish/delete
    │   ├── course-form/           create/edit course metadata (reused for both)
    │   └── course-curriculum/     add/rename/delete sections and lessons inline
    └── dashboard/                placeholder — proves the auth loop works end to end
```

## Routes

| Path                                  | Guard                        | Notes                                  |
|----------------------------------------|-------------------------------|------------------------------------------|
| `/courses`                            | none                          | Public catalog                          |
| `/courses/:slug`                      | none                          | Public course detail                    |
| `/dashboard`                          | authGuard                     |                                          |
| `/instructor/courses`                 | authGuard + roleGuard(INSTRUCTOR) | List own courses                    |
| `/instructor/courses/new`             | authGuard + roleGuard(INSTRUCTOR) | Create course                       |
| `/instructor/courses/:id/edit`        | authGuard + roleGuard(INSTRUCTOR) | Edit course metadata                |
| `/instructor/courses/:id/curriculum`  | authGuard + roleGuard(INSTRUCTOR) | Manage sections/lessons             |

## What's next

- Enrollment: the "Enroll now" button on course detail is currently disabled — wire it
  up once `enrollment-service` exists
- A real dashboard (enrolled courses, progress) replacing the placeholder
- `reset-password` page (the route from the emailed link — not built yet, only
  `forgot-password` which requests the token)
- Admin screens (user/course/category management) once `admin` concerns are decided
- File upload for course thumbnails/videos — currently just accepts a pasted URL

## Known trade-offs (portfolio-project scope, worth knowing about)

- **Tokens live in `localStorage`** (see `TokenStorageService`), which is simple but
  readable by any JS on the page. A hardening step for a "real" deployment would move
  the refresh token to an httpOnly cookie set by the backend.
- **`CourseService.getMineById()` fetches all of an instructor's courses and filters
  client-side** — fine at small scale, but worth adding a dedicated
  `GET /courses/mine/{id}` endpoint on the backend if instructors accumulate hundreds
  of courses.
- **No E2E or component tests yet** — Karma/Jasmine scaffolding is in place
  (`npm test`) but no specs have been written beyond the CLI defaults.
