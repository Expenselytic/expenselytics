import { Injectable, NgZone, OnDestroy } from "@angular/core";
import { BehaviorSubject, filter, Observable, Subscription } from "rxjs";
import { ApiRoot } from "./api-root";
import { HttpClient } from "@angular/common/http";

@Injectable({providedIn: 'root'})
export class ApiRootService implements OnDestroy{
    private readonly apiRoot$ = new BehaviorSubject<ApiRoot | undefined>(undefined);

    private readonly subscription = new Subscription();
    constructor(private zone: NgZone, private readonly http: HttpClient) {
        this.zone.runOutsideAngular(() => {
            const sub = this.http.get<ApiRoot>('http://localhost:8080/api/v1').subscribe({
                next: (root) => this.apiRoot$.next(root),
                error: (err) => this.apiRoot$.error(err)
            });
            this.subscription.add(sub);
        })
    }

    getApiRoot(): Observable<ApiRoot> {
        return this.apiRoot$.asObservable().pipe(
            filter(r => !!r)
        );
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }
}