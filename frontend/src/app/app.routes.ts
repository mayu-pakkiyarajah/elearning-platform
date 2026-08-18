import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout.component';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';

export const routes: Routes = [
  {
    path: 'auth',
    component: AuthLayoutComponent,
    canActivate: [guestGuard],
    children: [
      { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
      { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },
      { path: 'forgot-password', loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
      { path: '', redirectTo: 'login', pathMatch: 'full' },
    ],
  },
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      // Public catalog — no login required to browse.
      { path: 'courses', loadComponent: () => import('./features/courses/course-list/course-list.component').then(m => m.CourseListComponent) },
      { path: 'courses/:slug', loadComponent: () => import('./features/courses/course-detail/course-detail.component').then(m => m.CourseDetailComponent) },

      // Authenticated
      { path: 'dashboard', canActivate: [authGuard], loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'learn/:slug', canActivate: [authGuard], loadComponent: () => import('./features/learning/lesson-viewer/lesson-viewer.component').then(m => m.LessonViewerComponent) },

      // Instructor-only course management
      {
        path: 'instructor/courses',
        canActivate: [authGuard, roleGuard(['ROLE_INSTRUCTOR'])],
        children: [
          { path: '', loadComponent: () => import('./features/instructor/my-courses/my-courses.component').then(m => m.MyCoursesComponent) },
          { path: 'new', loadComponent: () => import('./features/instructor/course-form/course-form.component').then(m => m.CourseFormComponent) },
          { path: ':id/edit', loadComponent: () => import('./features/instructor/course-form/course-form.component').then(m => m.CourseFormComponent) },
          { path: ':id/curriculum', loadComponent: () => import('./features/instructor/course-curriculum/course-curriculum.component').then(m => m.CourseCurriculumComponent) },
        ],
      },

      { path: '', redirectTo: 'courses', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: 'courses' },
];
