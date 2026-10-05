import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Subject, tap } from 'rxjs';
import { AUTH_API_ROOT } from './api/api-paths';
import { profileFromResponse } from './api/profile';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly signedOut = new Subject<void>();

  invalidate(): void {
    this.signedOut.next();
  }

  constructor(private readonly http: HttpClient) {}

  current() {
    return this.http
      .get(`${AUTH_API_ROOT}/me`)
      .pipe(map(profileFromResponse));
  }

  authenticate(
    mode: 'login' | 'signup',
    name: string,
    email: string,
    password: string,
  ) {
    return this.http
      .post(`${AUTH_API_ROOT}/${mode}`, {
        name,
        email,
        password,
      })
      .pipe(map(profileFromResponse));
  }

  logout() {
    return this.http.post(`${AUTH_API_ROOT}/logout`, {}).pipe(
      tap(() => this.invalidate()),
      map(() => undefined),
    );
  }
}
