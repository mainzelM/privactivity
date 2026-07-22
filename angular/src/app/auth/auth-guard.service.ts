import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn, RouterStateSnapshot} from '@angular/router';
import {Router} from '@angular/router';
import {AuthService} from './auth.service';
import {Observable, of, switchMap, take, tap} from 'rxjs';

export const authGuard: CanActivateFn = (next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.isLoggedIn().pipe(
        take(1),
        switchMap(loggedIn =>
            loggedIn
                ? authService.isUserInRole(next.routeConfig?.data?.['role']).pipe(take(1))
                : of(false)
        ),
        tap(allowed => {
            if (!allowed) {
                console.log("Access denied - User not logged in or does not have the required role.");
                router.navigateByUrl("/login");
            }
        })
    );
};
