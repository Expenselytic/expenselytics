import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { Profile } from './api/profile';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;
  const profile: Profile = {
    userId: 42,
    name: 'Jane',
    email: 'jane@example.com',
    createdAt: '2026-10-04T12:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads and maps the current profile through the v1 API', () => {
    let result: Profile | undefined;
    service.current().subscribe((value) => (result = value));
    const request = http.expectOne('/api/v1/auth/me');
    expect(request.request.method).toBe('GET');
    request.flush({ ...profile, internalField: 'excluded' });
    expect(result).toEqual(profile);
  });

  for (const mode of ['signup', 'login'] as const) {
    it(`sends ${mode} credentials to the v1 API`, () => {
      let result: Profile | undefined;
      service
        .authenticate(
          mode,
          profile.name,
          profile.email,
          'long-password-123',
        )
        .subscribe((value) => (result = value));
      const request = http.expectOne(`/api/v1/auth/${mode}`);
      expect(request.request.method).toBe('POST');
      expect(request.request.body).toEqual({
        name: profile.name,
        email: profile.email,
        password: 'long-password-123',
      });
      request.flush(profile);
      expect(result).toEqual(profile);
    });
  }

  it('maps a successful empty logout response to void', () => {
    let completed = false;
    service.logout().subscribe((value) => {
      expect(value).toBeUndefined();
      completed = true;
    });
    const request = http.expectOne('/api/v1/auth/logout');
    expect(request.request.method).toBe('POST');
    request.flush(null, { status: 204, statusText: 'No Content' });
    expect(completed).toBeTrue();
  });

  it('rejects malformed profiles during response mapping', () => {
    let failure: Error | undefined;
    service.current().subscribe({ error: (error) => (failure = error) });
    http.expectOne('/api/v1/auth/me').flush({ name: 'Jane' });
    expect(failure?.message).toBe('The API returned an invalid profile.');
  });

  it('preserves login HTTP errors', () => {
    let status: number | undefined;
    service
      .authenticate('login', '', profile.email, 'wrong')
      .subscribe({ error: (error) => (status = error.status) });
    http
      .expectOne('/api/v1/auth/login')
      .flush(
        { detail: 'Invalid email or password' },
        { status: 401, statusText: 'Unauthorized' },
      );
    expect(status).toBe(401);
  });
});
