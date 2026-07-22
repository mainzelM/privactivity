import {ComponentFixture, TestBed} from '@angular/core/testing';

import {LoginComponent} from './login.component';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {provideRouter} from '@angular/router';
import {AuthService} from '../auth/auth.service';
import {of} from 'rxjs';

class AuthServiceStub {
    login() {
        return of(void 0);
    }

    logout() {
        // no-op
    }
}

describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                LoginComponent,
                MatSnackBarModule,
                BrowserAnimationsModule
            ],
            providers: [
                provideRouter([]),
                {provide: AuthService, useClass: AuthServiceStub}
            ]
        })
            .compileComponents();

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('password should be empty by default', () => {
        expect(component.password).toBe('');
    })
});
