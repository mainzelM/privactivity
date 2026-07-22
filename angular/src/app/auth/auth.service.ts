import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable, switchMap} from 'rxjs';
import {environment} from "../../environments/environment";
import {jwtDecode, JwtPayload} from "jwt-decode";
import {AuthTokenWorkerService} from "./auth-token-worker.service";

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    constructor(private http: HttpClient, private tokenWorker: AuthTokenWorkerService) {
    }

    isLoggedIn(): Observable<boolean> {
        return this.tokenWorker.token$.pipe(map(token => token != null));
    }

    login(username: string, password: string): Observable<void> {
        return this.http.get(environment.apiUrl + "/auth/csrf", {responseType: 'text'}).pipe(
            switchMap(csrfToken => this.http.post(environment.apiUrl + "/auth", null, {
                headers: {
                    Authorization: 'Basic ' + window.btoa(username + ':' + password),
                    'X-XSRF-TOKEN': csrfToken
                },
                responseType: 'text' as 'text',
            })),
            map((token: string) => {
                this.tokenWorker.setToken(token);

                const decodedToken = jwtDecode<JwtPayload>(token);

                // @ts-ignore
                this.tokenWorker.setRoles(decodedToken.scope);
            })
        );
    }

    logout(): void {
        this.tokenWorker.clear();
    }

    isUserInRole(roleFromRoute: string): Observable<boolean> {
        return this.tokenWorker.roles$.pipe(
            map(roles => {
                if (!roles) return false;
                if (!roles.includes(',')) return roles === roleFromRoute;
                return roles.split(',').includes(roleFromRoute);
            })
        );
    }
}
