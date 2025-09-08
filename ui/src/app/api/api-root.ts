import { Resource } from "../hal/resource";

export class ApiRoot extends Resource {
    apiVersion: string = 'unknown';

    status: string = 'unknown';

    constructor(obj: any) {
        super(obj);
        Object.assign(this, obj);
    }
}