export class ApiError  extends Error {
    readonly timestamp = new Date().toISOString();

    readonly httpStatus = 0;

    readonly path?: string;

    readonly exception?: string;

    constructor(obj: any) {
        super();
        Object.assign(this, obj);
        this.name = 'ApiError';
    }
}