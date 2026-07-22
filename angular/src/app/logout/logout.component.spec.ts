import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

import { LogoutComponent } from './logout.component';
import { AuthService } from '../auth/auth.service';

describe('LogoutComponent', () => {
    let component: LogoutComponent;
    let fixture: ComponentFixture<LogoutComponent>;

    let authService: jasmine.SpyObj<AuthService>;
    let router: jasmine.SpyObj<Router>;
    let snackBar: jasmine.SpyObj<MatSnackBar>;
    let originalCredentials: CredentialsContainer;

    beforeEach(async () => {
        authService = jasmine.createSpyObj<AuthService>('AuthService', ['logout']);
        router = jasmine.createSpyObj<Router>('Router', ['navigateByUrl']);
        snackBar = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']);

        originalCredentials = navigator.credentials;

        await TestBed.configureTestingModule({
            imports: [LogoutComponent],
            providers: [
                { provide: AuthService, useValue: authService },
                { provide: Router, useValue: router },
                { provide: MatSnackBar, useValue: snackBar }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(LogoutComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    afterEach(() => {
        Object.defineProperty(navigator, 'credentials', {
            value: originalCredentials,
            configurable: true
        });
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should logout, prevent silent sign-in and navigate to login', () => {
        const preventSilentAccess = jasmine.createSpy('preventSilentAccess').and.returnValue(Promise.resolve());
        Object.defineProperty(navigator, 'credentials', {
            value: {
                preventSilentAccess
            } as Pick<CredentialsContainer, 'preventSilentAccess'>,
            configurable: true
        });

        component.logout();

        expect(authService.logout).toHaveBeenCalled();
        expect(preventSilentAccess).toHaveBeenCalled();
        expect(snackBar.open).toHaveBeenCalledWith('Logout successful', 'OK', { duration: 5000 });
        expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
    });

    it('should continue logout when credentials api is unavailable', () => {
        Object.defineProperty(navigator, 'credentials', {
            value: undefined,
            configurable: true
        });

        component.logout();

        expect(authService.logout).toHaveBeenCalled();
        expect(snackBar.open).toHaveBeenCalledWith('Logout successful', 'OK', { duration: 5000 });
        expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
    });
});
