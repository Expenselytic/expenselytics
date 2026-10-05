import { TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { routes } from './app.routes';
import { Resource } from './hal/resource';
import { ResourceService } from './hal/resource.service';
import { AuthService } from './auth.service';
import { ApiRoot } from './api/api-root';

describe('Workspace routing', () => {
  beforeEach(() => {
    const root = jasmine.createSpyObj<ApiRoot>('root', ['getExpenses', 'getSavings']);
    root.getExpenses.and.returnValue(of([])); root.getSavings.and.returnValue(of([]));
    spyOn(Resource, 'fetchRoot').and.returnValue(of(root));
    TestBed.configureTestingModule({ providers: [provideRouter(routes),
      { provide: ResourceService, useValue: {} },
      { provide: HttpClient, useValue: { get: () => of([]) } },
      { provide: AuthService, useValue: { current: () => of({ name: 'Jane', email: 'jane@example.com' }) } },
    ] });
  });
  it('navigates from the home CTA to /workspace and back home', async () => {
    const harness = await RouterTestingHarness.create('/');
    expect(harness.routeNativeElement?.querySelector('.workspace-layout')).toBeNull();
    (harness.routeNativeElement?.querySelector('.hero-actions .button') as HTMLButtonElement).click();
    await harness.fixture.whenStable(); harness.detectChanges();
    expect(TestBed.inject(Router).url).toBe('/workspace');
    expect(harness.routeNativeElement?.querySelector('.workspace-sidebar')).toBeTruthy();
    expect(harness.routeNativeElement?.querySelector('app-analytics')).toBeTruthy();
    (harness.routeNativeElement?.querySelector('.home-link') as HTMLAnchorElement).click();
    await harness.fixture.whenStable(); harness.detectChanges();
    expect(TestBed.inject(Router).url).toBe('/');
    expect(harness.routeNativeElement?.querySelector('.hero')).toBeTruthy();
  });
  it('supports a direct workspace URL and section navigation', async () => {
    const harness = await RouterTestingHarness.create('/workspace');
    expect(harness.routeNativeElement?.querySelector('#experiments')).toBeTruthy();
    const link = harness.routeNativeElement?.querySelector('.workspace-sidebar a[href="/workspace#savings"]') as HTMLAnchorElement;
    link.click(); await harness.fixture.whenStable(); harness.detectChanges();
    expect(TestBed.inject(Router).url).toBe('/workspace#savings');
    expect(harness.routeNativeElement?.querySelector('#savings')).toBeTruthy();
  });
});
