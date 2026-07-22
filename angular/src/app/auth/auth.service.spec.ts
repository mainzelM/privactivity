import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';

import {AuthService} from './auth.service';
import {AuthTokenWorkerService} from './auth-token-worker.service';
import {of} from 'rxjs';

describe('AuthService', () => {
    let service: AuthService;

    beforeEach(() => {
        const mockTokenWorker: Partial<AuthTokenWorkerService> = {
            token$: of(null),
            roles$: of(null),
            setToken: jasmine.createSpy('setToken'),
            setRoles: jasmine.createSpy('setRoles'),
            clear: jasmine.createSpy('clear'),
        };

        TestBed.configureTestingModule({
            providers: [
                provideHttpClient(),
                provideHttpClientTesting(),
                {provide: AuthTokenWorkerService, useValue: mockTokenWorker}
            ]
        });
        service = TestBed.inject(AuthService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
