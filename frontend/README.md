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
    │   └── course-detail/        full detail page, curriculum accordion, live enroll button
    ├── learning/                 requires login (authGuard)
    │   ├── lesson-viewer/         sidebar + content pane, mark-complete, auto-advance, quiz links
    │   └── quiz-take/             answer questions, submit, see per-question correct/incorrect
    ├── instructor/               requires ROLE_INSTRUCTOR (roleGuard)
    │   ├── my-courses/            instructor's own courses, incl. drafts; publish/unpublish/delete
    │   ├── course-form/           create/edit course metadata (reused for both)
    │   ├── course-curriculum/     add/rename/delete sections and lessons inline
    │   └── quiz-manager/          create quizzes, add/remove questions with choices inline
    ├── certificates/
    │   ├── my-certificates/       requires login — download own PDFs, copy share links
    │   └── certificate-verify/    PUBLIC — no login required, what a QR code/share link opens
    └── dashboard/                student: enrolled courses with progress + certificate button; instructor: shortcut to My Courses
```

## Routes

| Path                                  | Guard                        | Notes                                  |
|----------------------------------------|-------------------------------|------------------------------------------|
| `/courses`                            | none                          | Public catalog                          |
| `/courses/:slug`                      | none                          | Public course detail, live enroll button|
| `/certificates/verify/:code`          | none                          | Public — what a certificate's QR code / share link opens |
| `/learn/:slug`                        | authGuard                     | Lesson viewer — 404s to "not enrolled" if you haven't enrolled |
| `/learn/:slug/quizzes/:quizId`        | authGuard                     | Take a quiz, see scored results         |
| `/certificates`                       | authGuard                     | Your own certificates — download, copy share link |
| `/dashboard`                          | authGuard                     | Student: enrollments + certificate button; instructor: shortcut |
| `/instructor/courses`                 | authGuard + roleGuard(INSTRUCTOR) | List own courses                    |
| `/instructor/courses/new`             | authGuard + roleGuard(INSTRUCTOR) | Create course                       |
| `/instructor/courses/:id/edit`        | authGuard + roleGuard(INSTRUCTOR) | Edit course metadata                |
| `/instructor/courses/:id/curriculum`  | authGuard + roleGuard(INSTRUCTOR) | Manage sections/lessons             |
| `/instructor/courses/:id/quizzes`     | authGuard + roleGuard(INSTRUCTOR) | Manage quizzes/questions            |

## What's next

- A real dashboard widget for instructors (enrollment counts per course, using
  enrollment-service's `GET /enrollments/course/{courseId}` roster endpoint; quiz
  pass rates, using quiz-service's `GET /quizzes/{id}/submissions`)
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
- **The lesson viewer doesn't enforce locking server-side** — course-service returns
  full lesson content (including `videoUrl`/`textContent`) regardless of enrollment;
  only the UI hides non-preview lessons behind a 🔒 on the course detail page. A
  determined user could read the raw API response. Real access control for paid
  content would need course-service or a gateway to check enrollment before returning
  lesson bodies — worth doing before this goes anywhere with real paid courses.
- **The quiz manager's "add choice" form is plain, unstyled radio buttons** rather
  than a polished picker — functional but the least visually refined screen in the
  app; worth a pass if this becomes a real demo.
- **Certificate downloads go through a blob fetch + synthetic `<a click>`** (see
  `MyCertificatesComponent.download`) since the browser needs to send the Bearer
  token, which a plain `<a href>` can't do. The public verify page's PDF link is a
  plain `<a href>` instead, since that endpoint needs no auth at all.
- **No E2E or component tests yet** — Karma/Jasmine scaffolding is in place
  (`npm test`) but no specs have been written beyond the CLI defaults.
