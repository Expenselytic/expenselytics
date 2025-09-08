import { Inject, Injectable } from "@angular/core";
import { HAL_CONFIG, HalConfig } from "./hal.module";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { catchError, map, Observable, throwError } from "rxjs";
import { Resource } from "./resource";
import { ApiError } from "./api.error";

@Injectable()
export class ResourceService {
    private readonly root: string;

    constructor(@Inject(HAL_CONFIG) config: HalConfig,
        private readonly httpClient: HttpClient) {
            this.root = config.apiRoot;
    }
    get<T extends Resource>(type: new (x: any) => T, uri: string): Observable<T> {
        return this.httpClient.get<any>(uri).pipe(
            map(obj => Resource.create(type, obj)),
            map(r => this.ensureSelf(r, uri)),
            catchError(err => this.handleError(err))
        );
    }

    getRoot<T extends Resource>(type: new (x: any) => T): Observable<T> {
        return this.get(type, this.root);
    }

    private ensureSelf<T extends Resource>(resource: T, uri: string): T {
        if (!resource.hasLink('self')) {
            resource._links['self'] = { href: uri };
        }

        return resource;
    }

    private handleError(err: HttpErrorResponse): Observable<never> {
        if(err.error instanceof ErrorEvent || typeof err.error === 'string') {
            return throwError(() => new ApiError({
                message: err.error.message || err.error,
                status: err.status,
                statusText: err.statusText
            }))
        }

        return throwError(() => new ApiError({
            ...err.error,
            httpStatus: err.status,    
        }));
    }
}