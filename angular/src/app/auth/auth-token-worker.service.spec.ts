import {AuthTokenWorkerService} from './auth-token-worker.service';
import {environment} from '../../environments/environment';

class MessagePortStub {
    onmessage: ((event: MessageEvent) => void) | null = null;
    readonly postMessage = jasmine.createSpy('postMessage');
    readonly start = jasmine.createSpy('start');
}

class SharedWorkerStub {
    constructor(public readonly url: URL, public readonly port: MessagePortStub) {
    }
}

describe('AuthTokenWorkerService', () => {
    const originalSharedWorker = globalThis.SharedWorker;
    const originalUseSharedAuthTokenWorker = environment.useSharedAuthTokenWorker;

    let port: MessagePortStub;
    let sharedWorkerCtorSpy: jasmine.Spy;

    beforeEach(() => {
        port = new MessagePortStub();
        const SharedWorkerCtor = function (this: unknown, url: URL) {
            return new SharedWorkerStub(url, port);
        };
        sharedWorkerCtorSpy = spyOn({SharedWorkerCtor}, 'SharedWorkerCtor').and.callThrough();

        Object.defineProperty(globalThis, 'SharedWorker', {
            configurable: true,
            writable: true,
            value: sharedWorkerCtorSpy
        });
        localStorage.removeItem('auth.token');
        localStorage.removeItem('auth.roles');
        environment.useSharedAuthTokenWorker = true;
    });

    afterEach(() => {
        Object.defineProperty(globalThis, 'SharedWorker', {
            configurable: true,
            writable: true,
            value: originalSharedWorker
        });
        environment.useSharedAuthTokenWorker = originalUseSharedAuthTokenWorker;
        localStorage.removeItem('auth.token');
        localStorage.removeItem('auth.roles');
    });

    it('should publish local auth updates immediately', () => {
        const testee = new AuthTokenWorkerService();
        let latestToken: string | null | undefined;
        let latestRoles: string | null | undefined;

        testee.token$.subscribe(token => latestToken = token);
        testee.roles$.subscribe(roles => latestRoles = roles);

        testee.setToken('token-123');
        testee.setRoles('ROLE_USER');

        expect(latestToken).toBe('token-123');
        expect(latestRoles).toBe('ROLE_USER');
        expect(port.postMessage).toHaveBeenCalledWith({type: 'GET_STATE'});
        expect(port.postMessage).toHaveBeenCalledWith({type: 'SET_TOKEN', value: 'token-123'});
        expect(port.postMessage).toHaveBeenCalledWith({type: 'SET_ROLES', value: 'ROLE_USER'});
    });

    it('should clear local auth state immediately', () => {
        const testee = new AuthTokenWorkerService();
        let latestToken: string | null | undefined;
        let latestRoles: string | null | undefined;

        testee.token$.subscribe(token => latestToken = token);
        testee.roles$.subscribe(roles => latestRoles = roles);

        testee.setToken('token-123');
        testee.setRoles('ROLE_USER');
        testee.clear();

        expect(latestToken).toBeNull();
        expect(latestRoles).toBeNull();
        expect(port.postMessage).toHaveBeenCalledWith({type: 'CLEAR'});
    });

    it('should apply worker state updates', () => {
        const testee = new AuthTokenWorkerService();
        let latestToken: string | null | undefined;
        let latestRoles: string | null | undefined;

        testee.token$.subscribe(token => latestToken = token);
        testee.roles$.subscribe(roles => latestRoles = roles);

        port.onmessage?.({
            data: {type: 'STATE', token: 'worker-token', roles: 'ROLE_ADMIN'}
        } as MessageEvent);

        expect(latestToken).toBe('worker-token');
        expect(latestRoles).toBe('ROLE_ADMIN');
    });

    it('should persist auth state in local storage when worker backend is disabled', () => {
        environment.useSharedAuthTokenWorker = false;

        const testee = new AuthTokenWorkerService();
        testee.setToken('dev-token');
        testee.setRoles('ROLE_DEV');

        const secondInstance = new AuthTokenWorkerService();
        let latestToken: string | null | undefined;
        let latestRoles: string | null | undefined;

        secondInstance.token$.subscribe(token => latestToken = token);
        secondInstance.roles$.subscribe(roles => latestRoles = roles);

        expect(sharedWorkerCtorSpy).not.toHaveBeenCalled();
        expect(latestToken).toBe('dev-token');
        expect(latestRoles).toBe('ROLE_DEV');
    });
});
