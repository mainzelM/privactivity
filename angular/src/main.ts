import { provideZoneChangeDetection } from "@angular/core";
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideRouter } from '@angular/router';
import routeConfig from './app/routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from "@angular/common/http";
import { authInterceptor } from "./app/auth/auth.interceptor";

bootstrapApplication(AppComponent, {
    providers: [
        provideZoneChangeDetection(),provideRouter(routeConfig),
        provideAnimationsAsync(),
        provideHttpClient(
            withInterceptors([authInterceptor]),
        ),
    ],
}).catch((err) => console.error(err));