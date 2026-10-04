import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiRoot } from './api-root';
import { ApiRootService } from './api-root.service';

describe('ApiRootService', () => {
  it('maps the v1 response to a usable HAL root', () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const service = TestBed.inject(ApiRootService);
    const http = TestBed.inject(HttpTestingController);
    let result: ApiRoot | undefined;
    service.getApiRoot().subscribe((root) => (result = root));
    http.expectOne('/api/v1').flush({
      apiVersion: '1.0',
      status: 'OK',
      _links: { self: { href: '/api/v1' } },
    });
    expect(result).toBeInstanceOf(ApiRoot);
    expect(result?.hrefFor('self')).toBe('/api/v1');
    expect(result?.apiVersion).toBe('1.0');
    service.ngOnDestroy();
    http.verify();
  });
});
