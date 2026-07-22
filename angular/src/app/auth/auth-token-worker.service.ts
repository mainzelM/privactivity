import {Injectable} from '@angular/core';
import {map, Observable, ReplaySubject} from 'rxjs';

interface AuthState {
    token: string | null;
    roles: string | null;
}

@Injectable({
    providedIn: 'root'
})
export class AuthTokenWorkerService {

    private readonly port: MessagePort;
    private readonly state$ = new ReplaySubject<AuthState>(1);
    private currentState: AuthState = {token: null, roles: null};

    readonly token$: Observable<string | null> = this.state$.pipe(map(s => s.token));
    readonly roles$: Observable<string | null> = this.state$.pipe(map(s => s.roles));

    constructor() {
        const worker = new SharedWorker(new URL('./auth-token.worker', import.meta.url));
        this.port = worker.port;
        this.port.onmessage = ({data}) => {
            if (data.type === 'STATE') {
                this.updateState({token: data.token, roles: data.roles});
            }
        };
        this.port.start();
        this.port.postMessage({type: 'GET_STATE'});
    }

    setToken(token: string): void {
        this.updateState({token});
        this.port.postMessage({type: 'SET_TOKEN', value: token});
    }

    setRoles(roles: string): void {
        this.updateState({roles});
        this.port.postMessage({type: 'SET_ROLES', value: roles});
    }

    clear(): void {
        this.updateState({token: null, roles: null});
        this.port.postMessage({type: 'CLEAR'});
    }

    private updateState(stateUpdate: AuthState | Partial<AuthState>): void {
        this.currentState = {...this.currentState, ...stateUpdate};
        this.state$.next(this.currentState);
    }
}
