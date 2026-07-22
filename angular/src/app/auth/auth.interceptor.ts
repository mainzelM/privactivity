import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {catchError, switchMap, take, throwError} from 'rxjs';
import {inject} from '@angular/core';
import {Router} from '@angular/router';
import {AuthTokenWorkerService} from './auth-token-worker.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const router = inject(Router);
    const tokenWorker = inject(AuthTokenWorkerService);

    return tokenWorker.token$.pipe(
        take(1),
        switchMap(token => {
            const newReq = token
                ? req.clone({headers: req.headers.set("Authorization", "Bearer " + token)})
                : req;

            return next(newReq).pipe(
                catchError((error: HttpErrorResponse) => {
                    console.log("Handling error response:", error);
                    if (error.status === 401) {
                        router.navigateByUrl("/login", {replaceUrl: true});
                    }
                    return throwError(() => error);
                })
            );
        })
    );
};
