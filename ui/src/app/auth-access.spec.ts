import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController,
} from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of, Subject, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { routes } from './app.routes';
import { Resource } from './hal/resource';
import { ResourceService } from './hal/resource.service';
import { ApiRoot } from './api/api-root';
import { LoginPage } from './login-page';
import { safeReturnUrl } from './auth.guard';
import { sessionInterceptor } from './auth.interceptor';

describe('Login access', () => {
  let auth: jasmine.SpyObj<AuthService>;
  let signedOut: Subject<void>;
  beforeEach(() => {
    signedOut = new Subject<void>();
    auth = jasmine.createSpyObj<AuthService>(
      'auth',
      ['current', 'authenticate', 'logout', 'invalidate'],
      { signedOut },
    );
    auth.current.and.returnValue(throwError(() => ({ status: 401 })));
    TestBed.configureTestingModule({
      providers: [
        provideRouter(routes),
        { provide: AuthService, useValue: auth },
        { provide: ResourceService, useValue: {} },
        { provide: HttpClient, useValue: { get: () => of([]) } },
      ],
    });
    spyOn(Resource, 'fetchRoot');
  });

  it('keeps signed-out home free of finance data', async () => {
    const harness = await RouterTestingHarness.create('/');
    expect(Resource.fetchRoot).not.toHaveBeenCalled();
    expect(
      harness.routeNativeElement?.querySelector('.home-summary'),
    ).toBeNull();
    expect(
      harness.routeNativeElement?.querySelector('.hero-card'),
    ).toBeNull();
    const explore = harness.routeNativeElement?.querySelector(
      '.explore-link',
    ) as HTMLAnchorElement;
    explore.click();
    await harness.fixture.whenStable();
    expect(TestBed.inject(Router).url).toContain('/login?');
  });

  it('returns to the workspace after login', async () => {
    const harness = await RouterTestingHarness.create(
      '/workspace#savings',
    );
    const router = TestBed.inject(Router);
    expect(router.url).toContain('/login?returnUrl=');
    const profile = {
      userId: 1,
      name: 'Jane',
      email: 'jane@example.com',
      createdAt: '2026-01-01T00:00:00Z',
    };
    const root = jasmine.createSpyObj<ApiRoot>('root', [
      'getExpenses',
      'getSavings',
    ]);
    root.getExpenses.and.returnValue(of([]));
    root.getSavings.and.returnValue(of([]));
    (Resource.fetchRoot as jasmine.Spy).and.returnValue(of(root));
    auth.current.and.returnValue(of(profile));
    auth.authenticate.and.returnValue(of(profile));
    const page = harness.routeDebugElement
      ?.componentInstance as LoginPage;
    page.email = profile.email;
    page.password = '12345678';
    page.submit();
    await harness.fixture.whenStable();
    harness.detectChanges();
    expect(router.url).toBe('/workspace#savings');
    expect(
      harness.routeNativeElement?.querySelector('.workspace-layout'),
    ).toBeTruthy();
    signedOut.next();
    harness.detectChanges();
    expect(
      harness.routeNativeElement?.querySelector('.workspace-layout'),
    ).toBeNull();
  });

  it('does not accept an external post-login destination', () => {
    expect(safeReturnUrl('https://example.com')).toBe('/workspace');
    expect(safeReturnUrl('//example.com')).toBe('/workspace');
    expect(safeReturnUrl('/workspace#savings')).toBe(
      '/workspace#savings',
    );
  });
});

describe('Expired financial session', () => {
  it('clears the session and redirects a rejected API request', () => {
    const auth = jasmine.createSpyObj<AuthService>('auth', [
      'invalidate',
    ]);
    const router = jasmine.createSpyObj<Router>('router', ['navigate'], {
      url: '/workspace#expenses',
    });
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([sessionInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
    TestBed.inject(HttpClient)
      .get('/api/v1/expenses')
      .subscribe({
        error: () => {},
      });
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/v1/expenses').flush(null, {
      status: 401,
      statusText: 'Unauthorized',
    });
    expect(auth.invalidate).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/workspace#expenses' },
    });
    http.verify();
  });
});
