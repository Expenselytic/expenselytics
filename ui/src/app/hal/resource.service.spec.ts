import { Injectable } from "@angular/core";
import { ResourceService } from "./resource.service";
import { Resource } from "./resource";
import { HalModule } from "./hal.module";
import { createHttpFactory, HttpMethod, SpectatorHttp } from '@ngneat/spectator';
import { provideHttpClientTesting } from "@angular/common/http/testing";
import { ApiError } from "./api.error";

@Injectable()
class TestService {
    constructor(public readonly resourceService: ResourceService) {}
}

class TestResource extends Resource { 
    public property = 'unset';

    constructor(obj: any) {
        super(obj);
        Object.assign(this, obj);
    }
}

describe('Resource and ResourceService', () => {
    const parent = Resource.create(TestResource, {
        property: 'parent',
        _links: {
            self: { href: '/api/parents/1' },
            plain: { href: '/api/plain' },
            templated: { href: '/api/templated/{id}{?q}', templated: true }
        },
        _embedded: {
            child: [
                { property: 'embedded-1' },
                { property: 'embedded-2' }
            ]
        }
    });

    const createService = createHttpFactory({
        service: TestService,
        imports: [HalModule.config({ apiRoot: '/api/v1' })],
        providers: [provideHttpClientTesting()]
    });

    let spectator: SpectatorHttp<TestService>;

    beforeEach(() => {
        spectator = createService();
    });

    describe('#fetchRoot', () => {
        it('should fetch the root resource', () => {
            let resource: TestResource | undefined;

            Resource.fetchRoot(TestResource).subscribe(r => resource = r);

            const req = spectator.expectOne('/api/v1', HttpMethod.GET);

            req.flush({
                property: 'set',
                _links: {
                    self: { href: '/api/v1' }
                }
            });

            expect(resource).toEqual(Resource.create(TestResource, {
                property: 'set',
                _links: {
                    self: { href: '/api/v1' }
                }
            }));
        });
    });

    describe('#get', () => {
        it('should fetch a resource and add self link if missing', () => {
            let resource: TestResource | undefined;

            spectator.service.resourceService.get(TestResource, '/api/test')
                .subscribe(r => resource = r);

            const req = spectator.expectOne('/api/test', HttpMethod.GET);

            req.flush({
                property: 'value',
                _links: {}
            });

            expect(resource).toEqual(Resource.create(TestResource, {
                property: 'value',
                _links: { self: { href: '/api/test' }}
            }));
        });

        it('should throw ApiError on HTTP error', () => {
            let error: any;

            spectator.service.resourceService.get(TestResource, '/api/error')
                .subscribe({
                    next: () => {},
                    error: (err) => error = err
                });

            const req = spectator.expectOne('/api/error', HttpMethod.GET);
            req.flush('Not Found', { status: 404, statusText: 'Not Found' });

            expect(error).toBeInstanceOf(ApiError);
            expect(error.status).toBe(404);
            expect(error.message).toBe('Not Found');
        });
    });

    describe('#getRoot', () => {
        it('should fetch the root resource', () => {
            let resource: TestResource | undefined;

            spectator.service.resourceService.getRoot(TestResource)
                .subscribe(r => resource = r);

            const req = spectator.expectOne('/api/v1', HttpMethod.GET);

            req.flush({
                property: 'root-value',
                _links: {}
            });

            expect(resource).toEqual(Resource.create(TestResource, {
                property: 'root-value',
                _links: { self: { href: '/api/v1' } }
            }));
        });
    });
});
