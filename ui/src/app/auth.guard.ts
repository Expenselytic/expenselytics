import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of, timeout } from 'rxjs';
import { AuthService } from './auth.service';

export const requireLogin: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.current().pipe(
    timeout(15000),
    map(() => true),
    catchError(() =>
      of(
        router.createUrlTree(['/login'], {
          queryParams: { returnUrl: state.url },
        }),
      ),
    ),
  );
};

export function safeReturnUrl(value: string | null): string {
  // Only return to known, local application pages.
  if (value === '/') return value;
  if (value && /^\/workspace(?:[?#].*)?$/.test(value)) return value;
  return '/workspace';
}
