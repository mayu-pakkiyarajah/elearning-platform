import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * - Attaches the access token to every request going to our own API.
 * - On a 401 (expired access token), tries exactly one silent refresh and
 *   replays the original request. If the refresh itself fails, the session
 *   is dead: clear it and send the user back to login.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isAuthEndpoint = /\/auth\/(login|register|refresh|forgot-password|reset-password)/.test(req.url);
  const token = authService.getAccessToken();

  const authorizedReq = token && !isAuthEndpoint
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authorizedReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const isUnauthorized = error.status === 401;
      const hasRefreshToken = !!authService.getRefreshToken();

      if (!isUnauthorized || isAuthEndpoint || !hasRefreshToken) {
        return throwError(() => error);
      }

      return authService.refresh().pipe(
        switchMap((res) => {
          const retriedReq = req.clone({
            setHeaders: { Authorization: `Bearer ${res.accessToken}` },
          });
          return next(retriedReq);
        }),
        catchError((refreshError) => {
          authService.logout();
          router.navigate(['/auth/login']);
          return throwError(() => refreshError);
        }),
      );
    }),
  );
};
