import { Observable } from "rxjs";
import { HalInjector } from "./hal.module";
import * as Template from 'url-template';
import { ResourceService } from "./resource.service";


export interface Link {
    href: string;
    templated?: boolean;
    type?: string;
    deprecation?: string;
    profile?: string;
    title?: string;
    hreflang?: string;
    name?: string;
}

export function array<T extends Resource>(type: new (x: any) => T,
    resource: Resource, rel = 'data'): T[] {
        const values = resource?.get(type, rel) as T[];

        return values || [];
}

export class Resource {
    static create<T extends Resource>(type: new (x: any) => T, obj: any): T {
        return new type(obj);
    }

    static fetchRoot<T extends Resource>(type: new (x: any) => T): Observable<T> {
        return HalInjector.get(ResourceService).getRoot(type);
    }

    readonly _links: { [key: string]: Link } = {};

    readonly _embedded: { [key: string]: any } = {};

    constructor(obj: any) {
        Object.assign(this, obj);
    }

    hasLink(rel: string): boolean {
        return !!this._links?.[rel];
    }

    fetch<T extends Resource>(type: new (x: any) => T, rel: string,
        params?: { [key: string]: any }): Observable<T> {
            const service = HalInjector.get(ResourceService);
            const uri = this.href(rel, params);
            
            return !uri ? this.error(`${rel} uri is undefined`) : service.get<T>(type, uri);
    }

    get<T extends Resource>(type: new (x: any) => T, rel: string): T | T[] | undefined {
        const obj= this._embedded?.[rel];

        if (!obj) {
            return undefined;
        }

        if (Array.isArray(obj)) {
            return obj.map(o => Resource.create(type, o));
        }

        return Resource.create(type, obj);
    }

    private error(message: string): Observable<any> {
        return new Observable(subscriber => subscriber.error(new Error(message)));
    }

    private href(rel: string, params: { [key: string]: any }= {}): string | undefined {
        const link = this.link(rel);

        if(!!link) {
            if(link.templated) {
                return Template.parseTemplate(link.href).expand(params);
            }

            return link.href;
        }

        return undefined;
    }

    private link(rel: string): Link | undefined {
        const link = this._links?.[rel];
        return !link ? undefined : link
    }
}