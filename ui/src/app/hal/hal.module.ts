import { InjectionToken, Injector, ModuleWithProviders, NgModule } from "@angular/core";
import { ResourceService } from "./resource.service";

export interface HalConfig {
    readonly apiRoot: string;
}

export const HAL_CONFIG = new InjectionToken<HalConfig>('hal.config');

export let HalInjector: Injector;

@NgModule()
export class HalModule {
    static config(config: HalConfig): ModuleWithProviders<HalModule> {
        return {
            ngModule: HalModule,
            providers: [
                { provide: HAL_CONFIG, useValue: config },
                ResourceService
            ]
        }
    }

    constructor(injector: Injector) {
        HalInjector = injector;
    }
}