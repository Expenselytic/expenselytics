import { Inject, Injectable } from '@angular/core';
import { HAL_CONFIG, HalConfig } from './hal.module';
import {
  HttpClient,
  HttpErrorResponse,
  HttpHeaders,
} from '@angular/common/http';
import { catchError, map, Observable, throwError } from 'rxjs';
import { Resource } from './resource';
import { ApiError } from './api.error';

@Injectable()
export class ResourceService {
  private readonly root: string;

  constructor(
    @Inject(HAL_CONFIG) config: HalConfig,
    private readonly httpClient: HttpClient,
  ) {
    this.root = config.apiRoot;
  }
  get<T extends Resource>(
    type: new (x: any) => T,
    uri: string,
  ): Observable<T> {
    const target = this.resolve(uri);
    return this.httpClient.get(target).pipe(
      map((obj) => Resource.create(type, obj)),
      map((r) => this.ensureSelf(r, target)),
      catchError((err) => this.handleError(err)),
    );
  }

  getRoot<T extends Resource>(type: new (x: any) => T): Observable<T> {
    return this.get(type, this.root);
  }

  post(uri: string, body: unknown): Observable<unknown> {
    return this.httpClient
      .post(this.resolve(uri), body, { headers: this.authHeaders() })
      .pipe(catchError((err) => this.handleError(err)));
  }

  put(uri: string, body: unknown): Observable<unknown> {
    return this.httpClient
      .put(this.resolve(uri), body, { headers: this.authHeaders() })
      .pipe(catchError((err) => this.handleError(err)));
  }

  delete(uri: string): Observable<void> {
    return this.httpClient
      .delete(this.resolve(uri), { headers: this.authHeaders() })
      .pipe(
        map(() => undefined),
        catchError((err) => this.handleError(err)),
      );
  }

  private authHeaders(): HttpHeaders {
    const token = globalThis.localStorage?.getItem('authToken');
    return token
      ? new HttpHeaders({ Authorization: `Bearer ${token}` })
      : new HttpHeaders();
  }

  private ensureSelf<T extends Resource>(resource: T, uri: string): T {
    if (!resource.hasLink('self')) {
      resource._links['self'] = { href: uri };
    }

    return resource;
  }

  private resolve(uri: string): string {
    if (uri.startsWith('/')) {
      return uri;
    }
    return new URL(
      uri,
      new URL(this.root, globalThis.location.origin),
    ).toString();
  }

  private handleError(err: HttpErrorResponse): Observable<never> {
    if (
      err.error instanceof ErrorEvent ||
      typeof err.error === 'string'
    ) {
      return throwError(
        () =>
          new ApiError({
            message: err.error.message || err.error,
            status: err.status,
            statusText: err.statusText,
          }),
      );
    }

    return throwError(
      () =>
        new ApiError({
          ...err.error,
          httpStatus: err.status,
        }),
    );
  }
}
