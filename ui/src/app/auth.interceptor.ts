import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const sessionInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const url = new URL(request.url, window.location.origin);
  const path = url.pathname;
  const privateApi =
    url.origin === window.location.origin &&
    path.startsWith('/api/v1/') &&
    !path.startsWith('/api/v1/auth/');
  return next(request).pipe(
    catchError((error) => {
      if (error.status === 401 && privateApi) {
        auth.invalidate();
        if (!router.url.startsWith('/login')) {
          void router.navigate(['/login'], {
            queryParams: { returnUrl: router.url },
          });
        }
      }
      return throwError(() => error);
    }),
  );
};
