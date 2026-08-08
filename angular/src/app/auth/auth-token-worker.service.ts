import {Injectable} from '@angular/core';
import {map, Observable, ReplaySubject} from 'rxjs';
import {environment} from '../../environments/environment';

interface AuthState {
    token: string | null;
    roles: string | null;
}

@Injectable({
    providedIn: 'root'
})
export class AuthTokenWorkerService {

    private static readonly tokenStorageKey = 'auth.token';
    private static readonly rolesStorageKey = 'auth.roles';

    private readonly port?: MessagePort;
    private readonly useWorkerBackend: boolean;
    private readonly state$ = new ReplaySubject<AuthState>(1);
    private currentState: AuthState = {token: null, roles: null};

    readonly token$: Observable<string | null> = this.state$.pipe(map(s => s.token));
    readonly roles$: Observable<string | null> = this.state$.pipe(map(s => s.roles));

    constructor() {
        this.useWorkerBackend = environment.useSharedAuthTokenWorker && typeof SharedWorker !== 'undefined';

        if (this.useWorkerBackend) {
            const worker = new SharedWorker(new URL('./auth-token.worker', import.meta.url));
            this.port = worker.port;
            this.port.onmessage = ({data}) => {
                if (data.type === 'STATE') {
                    this.updateState({token: data.token, roles: data.roles});
                }
            };
            this.port.start();
            this.port.postMessage({type: 'GET_STATE'});
        } else {
            this.currentState = {
                token: localStorage.getItem(AuthTokenWorkerService.tokenStorageKey),
                roles: localStorage.getItem(AuthTokenWorkerService.rolesStorageKey)
            };
            this.state$.next(this.currentState);
        }
    }

    setToken(token: string): void {
        this.updateState({token});
        if (this.useWorkerBackend) {
            this.port?.postMessage({type: 'SET_TOKEN', value: token});
        } else {
            localStorage.setItem(AuthTokenWorkerService.tokenStorageKey, token);
        }
    }

    setRoles(roles: string): void {
        this.updateState({roles});
        if (this.useWorkerBackend) {
            this.port?.postMessage({type: 'SET_ROLES', value: roles});
        } else {
            localStorage.setItem(AuthTokenWorkerService.rolesStorageKey, roles);
        }
    }

    clear(): void {
        this.updateState({token: null, roles: null});
        if (this.useWorkerBackend) {
            this.port?.postMessage({type: 'CLEAR'});
        } else {
            localStorage.removeItem(AuthTokenWorkerService.tokenStorageKey);
            localStorage.removeItem(AuthTokenWorkerService.rolesStorageKey);
        }
    }

    private updateState(stateUpdate: AuthState | Partial<AuthState>): void {
        this.currentState = {...this.currentState, ...stateUpdate};
        this.state$.next(this.currentState);
    }
}
