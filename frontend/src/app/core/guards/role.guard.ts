import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Usage in routes: canActivate: [roleGuard(['ROLE_ADMIN'])]
 */
export function roleGuard(allowedRoles: string[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const user = authService.currentUser();
    if (!user) {
      router.navigate(['/auth/login']);
      return false;
    }

    const hasAccess = user.roles.some((role) => allowedRoles.includes(role));
    if (!hasAccess) {
      router.navigate(['/dashboard']);
      return false;
    }

    return true;
  };
}
