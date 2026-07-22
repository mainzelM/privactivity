import {Component, DestroyRef, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Router} from '@angular/router';
import {AuthService} from '../auth/auth.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';

type PasswordCredentialRequestOptions = CredentialRequestOptions & {
    password?: boolean;
};

@Component({
    selector: 'app-login',
    standalone: true,
    templateUrl: './login.component.html',
    imports: [
        FormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule
    ],
    styleUrls: ['./login.component.css']
})
export class LoginComponent {
    private destroyRef = inject(DestroyRef);
    private authService = inject(AuthService);
    private router = inject(Router);
    private snackBar = inject(MatSnackBar);

    username: string = '';
    password: string = '';
    submitting = false;

    constructor() {
        this.prefillFromPasswordManager();
    }

    public login(): void {
        if (!this.username || !this.password || this.submitting) {
            return;
        }

        this.submitting = true;
        this.authService.logout();

        this.authService.login(this.username, this.password)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    this.storePasswordManagerCredential();
                    this.submitting = false;
                    this.router.navigateByUrl('/');
                },
                error: (error) => {
                    this.submitting = false;
                    this.snackBar.open(`Login failed: ${error.status}`, 'OK', {duration: 5000});
                }
            });
    }

    private prefillFromPasswordManager(): void {
        if (!navigator.credentials) {
            return;
        }

        const requestOptions: PasswordCredentialRequestOptions = {
            password: true,
            mediation: 'optional'
        };

        navigator.credentials.get(requestOptions)
            .then((credential: Credential | null) => {
                if (!credential) {
                    return;
                }

                const credentialWithPassword = credential as Credential & { id?: string; password?: string };
                if (credentialWithPassword.id) {
                    this.username = credentialWithPassword.id;
                }
                if (credentialWithPassword.password) {
                    this.password = credentialWithPassword.password;
                }
            })
            .catch(() => {
                // Ignore unsupported/user-dismissed credential prompts.
            });
    }

    private storePasswordManagerCredential(): void {
        const w = window as Window & typeof globalThis & {
            PasswordCredential?: new (data: { id: string; password: string }) => Credential;
        };

        if (!navigator.credentials || !w.PasswordCredential) {
            return;
        }

        try {
            const credential = new w.PasswordCredential({
                id: this.username,
                password: this.password
            });

            navigator.credentials.store(credential)
                .catch(() => {
                    // Ignore failures; successful login should continue.
                });
        } catch {
            // Ignore constructor failures in unsupported browsers.
        }
    }
}
