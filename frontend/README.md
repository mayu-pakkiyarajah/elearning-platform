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
│   ├── models/        User, AuthResponse, etc. — mirror the backend DTOs exactly
│   └── services/      AuthService, TokenStorageService
├── shared/
│   └── components/    alert, navbar — reused across features
├── layouts/
│   ├── auth-layout/    split-screen shell for login/register/forgot-password
│   └── main-layout/    navbar + router-outlet for authenticated pages
└── features/
    ├── auth/
    │   ├── login/
    │   ├── register/
    │   └── forgot-password/
    └── dashboard/       placeholder — proves the auth loop works end to end
```

## What's next

- `course-list` / `course-detail` features once `course-service` exists
- A real dashboard (enrolled courses, progress) replacing the placeholder
- `reset-password` page (the route from the emailed link — not built yet, only
  `forgot-password` which requests the token)
- Admin screens (user/course management) once `admin` concerns are decided

## Known trade-offs (portfolio-project scope, worth knowing about)

- **Tokens live in `localStorage`** (see `TokenStorageService`), which is simple but
  readable by any JS on the page. A hardening step for a "real" deployment would move
  the refresh token to an httpOnly cookie set by the backend.
- **No E2E or component tests yet** — Karma/Jasmine scaffolding is in place
  (`npm test`) but no specs have been written beyond the CLI defaults.
