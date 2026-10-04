import { Injectable, NgZone, OnDestroy } from '@angular/core';
import {
  BehaviorSubject,
  filter,
  map,
  Observable,
  Subscription,
} from 'rxjs';
import { ApiRoot } from './api-root';
import { HttpClient } from '@angular/common/http';
import { API_V1_ROOT } from './api-paths';

@Injectable({ providedIn: 'root' })
export class ApiRootService implements OnDestroy {
  private readonly apiRoot$ = new BehaviorSubject<ApiRoot | undefined>(
    undefined,
  );

  private readonly subscription = new Subscription();
  constructor(
    private zone: NgZone,
    private readonly http: HttpClient,
  ) {
    this.zone.runOutsideAngular(() => {
      const sub = this.http
        .get(API_V1_ROOT)
        .pipe(map((response) => new ApiRoot(response)))
        .subscribe({
          next: (root) => this.apiRoot$.next(root),
          error: (err) => this.apiRoot$.error(err),
        });
      this.subscription.add(sub);
    });
  }

  getApiRoot(): Observable<ApiRoot> {
    return this.apiRoot$.asObservable().pipe(filter((r) => !!r));
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
