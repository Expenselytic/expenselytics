import { TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { EMPTY, of } from 'rxjs';
import { routes } from './app.routes';
import { Resource } from './hal/resource';
import { ResourceService } from './hal/resource.service';
import { AuthService } from './auth.service';
import { ApiRoot } from './api/api-root';

describe('Workspace routing', () => {
  beforeEach(() => {
    const root = jasmine.createSpyObj<ApiRoot>('root', [
      'getExpenses',
      'getSavings',
    ]);
    root.getExpenses.and.returnValue(of([]));
    root.getSavings.and.returnValue(of([]));
    spyOn(Resource, 'fetchRoot').and.returnValue(of(root));
    TestBed.configureTestingModule({
      providers: [
        provideRouter(routes),
        { provide: ResourceService, useValue: {} },
        { provide: HttpClient, useValue: { get: () => of([]) } },
        {
          provide: AuthService,
          useValue: {
            signedOut: EMPTY,
            current: () =>
              of({ name: 'Jane', email: 'jane@example.com' }),
          },
        },
      ],
    });
  });
  it(
    'navigates from the home CTA to ' + '/workspace and back home',
    async () => {
      const harness = await RouterTestingHarness.create('/');
      expect(
        harness.routeNativeElement?.querySelector('.workspace-layout'),
      ).toBeNull();
      (
        harness.routeNativeElement?.querySelector(
          '.hero-actions .button',
        ) as HTMLButtonElement
      ).click();
      await harness.fixture.whenStable();
      harness.detectChanges();
      expect(TestBed.inject(Router).url).toBe('/workspace');
      expect(
        harness.routeNativeElement?.querySelector('.workspace-sidebar'),
      ).toBeTruthy();
      expect(
        harness.routeNativeElement?.querySelector('app-analytics'),
      ).toBeTruthy();
      (
        harness.routeNativeElement?.querySelector(
          '.home-link',
        ) as HTMLAnchorElement
      ).click();
      await harness.fixture.whenStable();
      harness.detectChanges();
      expect(TestBed.inject(Router).url).toBe('/');
      expect(
        harness.routeNativeElement?.querySelector('.hero'),
      ).toBeTruthy();
    },
  );
  it(
    'supports a direct workspace URL and ' + 'section navigation',
    async () => {
      const harness = await RouterTestingHarness.create('/workspace');
      expect(
        harness.routeNativeElement?.querySelector('#experiments'),
      ).toBeTruthy();
      const link = harness.routeNativeElement?.querySelector(
        '.workspace-sidebar a[href="/workspace#savings"]',
      ) as HTMLAnchorElement;
      link.click();
      await harness.fixture.whenStable();
      harness.detectChanges();
      expect(TestBed.inject(Router).url).toBe('/workspace#savings');
      expect(
        harness.routeNativeElement?.querySelector('#savings'),
      ).toBeTruthy();
    },
  );
  it(
    'opens the mobile dropdown, closes with ' +
      'Escape, and closes after section ' +
      'navigation',
    async () => {
      const harness = await RouterTestingHarness.create('/workspace');
      const toggle = harness.routeNativeElement?.querySelector(
        '.mobile-menu-toggle',
      ) as HTMLButtonElement;
      const panel = harness.routeNativeElement?.querySelector(
        '#mobile-navigation-panel',
      ) as HTMLElement;
      expect(toggle.getAttribute('aria-expanded')).toBe('false');
      expect(panel.hidden).toBeTrue();
      toggle.click();
      harness.detectChanges();
      expect(panel.hidden).toBeFalse();
      expect(toggle.getAttribute('aria-expanded')).toBe('true');
      document.dispatchEvent(
        new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }),
      );
      harness.detectChanges();
      expect(panel.hidden).toBeTrue();
      toggle.click();
      harness.detectChanges();
      (
        panel.querySelector(
          'a[href="/workspace#savings"]',
        ) as HTMLAnchorElement
      ).click();
      await harness.fixture.whenStable();
      harness.detectChanges();
      expect(TestBed.inject(Router).url).toBe('/workspace#savings');
      expect(panel.hidden).toBeTrue();
      toggle.click();
      harness.detectChanges();
      (
        harness.routeNativeElement?.querySelector(
          '.workspace-heading',
        ) as HTMLElement
      ).click();
      harness.detectChanges();
      expect(panel.hidden).toBeTrue();
    },
  );
});
