import {Component, DestroyRef, inject} from '@angular/core';
import {Router} from '@angular/router';

import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTabsModule} from '@angular/material/tabs';
import {MatListModule} from '@angular/material/list';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSnackBar} from '@angular/material/snack-bar';
import {
    AddedAndExistingTowns,
    AdminService,
    CacheResult,
    ChangePasswordRequest,
    ChangePasswordResult,
    ImportResult,
    RebuildTilesResult,
    SystemInfo
} from './admin.service';

@Component({
    selector: 'app-admin',
    imports: [
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatTabsModule,
    MatListModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule
],
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.scss'],
    host: {class: 'admin-component'}
})
export class AdminComponent {
    private destroyRef = inject(DestroyRef);
    adminService: AdminService = inject(AdminService);
    router: Router = inject(Router);
    private formBuilder: FormBuilder = inject(FormBuilder);
    private snackBar: MatSnackBar = inject(MatSnackBar);

    isLoading: boolean = false;
    systemInfo: SystemInfo | null = null;
    errors: string[] = [];
    removeAllBeforeImport: boolean = false;

    // Password change form
    passwordForm: FormGroup;

    constructor() {
        this.passwordForm = this.formBuilder.group({
            currentPassword: ['', [Validators.required]],
            newPassword: ['', [Validators.required, Validators.minLength(4)]],
            confirmPassword: ['', [Validators.required]]
        }, {validators: this.passwordMatchValidator});

        this.loadSystemInfo();
    }

    private passwordMatchValidator(control: AbstractControl): { [key: string]: any } | null {
        const newPassword = control.get('newPassword');
        const confirmPassword = control.get('confirmPassword');

        if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
            return {'passwordMismatch': true};
        }
        return null;
    }

    loadSystemInfo(): void {
        this.isLoading = true;
        this.adminService.getSystemInfo().subscribe({
            next: (info: SystemInfo) => {
                this.systemInfo = info;
                this.isLoading = false;
            },
            error: (error: any) => {
                console.error('Error loading system info:', error);
                this.errors.push('Failed to load system information');
                this.isLoading = false;
            }
        });
    }

    onImportAllActivities(): void {
        this.isLoading = true;
        this.errors = [];
        this.adminService.importAllActivities(this.removeAllBeforeImport).subscribe({
            next: (result: ImportResult) => {
                console.log('Import completed:', result);
                this.isLoading = false;
                // Optionally reload system info to show updated stats
                this.loadSystemInfo();
            },
            error: (error: any) => {
                console.error('Error importing activities:', error);
                this.errors.push('Failed to import activities');
                this.isLoading = false;
            }
        });
    }

    onClearCache(): void {
        this.isLoading = true;
        this.errors = [];
        this.adminService.clearCache().subscribe({
            next: (result: CacheResult) => {
                console.log('Cache cleared:', result);
                this.isLoading = false;
            },
            error: (error: any) => {
                console.error('Error clearing cache:', error);
                this.errors.push('Failed to clear cache');
                this.isLoading = false;
            }
        });
    }

    onAddMissingTowns(): void {
        this.isLoading = true;
        this.errors = [];
        this.adminService.addMissingTowns().subscribe({
            next: (result: AddedAndExistingTowns) => {
                console.log('Missing towns added:', result);
                this.isLoading = false;
            },
            error: (error: any) => {
                console.error('Error export missing towns:', error);
                this.errors.push('Failed to add missing towns');
                this.isLoading = false;
            }
        });
    }

    onRebuildTiles(): void {
        this.isLoading = true;
        this.errors = [];
        this.adminService.rebuildTiles().subscribe({
            next: (result: RebuildTilesResult) => {
                console.log('Tiles rebuilt:', result.generatedAt, result.tiles?.length ?? 0);
                this.isLoading = false;
            },
            error: (error: any) => {
                console.error('Error rebuilding tiles:', error);
                this.errors.push('Failed to rebuild tiles');
                this.isLoading = false;
            }
        });
    }

    onChangePassword(): void {
        if (!this.passwordForm.valid) {
            return;
        }

        this.isLoading = true;

        const request: ChangePasswordRequest = {
            currentPassword: this.passwordForm.get('currentPassword')?.value,
            newPassword: this.passwordForm.get('newPassword')?.value
        };

        this.adminService.changePassword(request)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (result: ChangePasswordResult) => {
                    this.isLoading = false;

                    if (result.success) {
                        const newPassword = this.passwordForm.get('newPassword')?.value;
                        this.updatePasswordManagerCredential(newPassword);
                        this.passwordForm.reset();
                        this.snackBar.open(result.message, 'Close', {duration: 5000});
                    } else {
                        this.snackBar.open(result.message, 'Close', {duration: 5000, panelClass: ['error-snackbar']});
                    }
                },
                error: (error: any) => {
                    console.error('Error changing password:', error);
                    this.isLoading = false;
                    this.snackBar.open('Failed to change password', 'Close', {
                        duration: 5000,
                        panelClass: ['error-snackbar']
                    });
                }
            });
    }

    private updatePasswordManagerCredential(newPassword: string): void {
        // Use the Credential Management API to inform the browser's password manager
        // about the password change so it can update stored credentials
        const w = window as any;
        if (!navigator.credentials || !w.PasswordCredential) {
            return;
        }

        try {
            const credential = new w.PasswordCredential({
                id: this.getCurrentUsername(),
                password: newPassword,
                name: 'Privactivity',
                iconURL: './favicon.ico'
            });

            navigator.credentials.store(credential)
                .then(() => console.log('Password manager updated with new credentials'))
                .catch((error: any) => console.debug('Could not update password manager:', error));
        } catch (error: any) {
            console.debug('Error creating password credential:', error);
        }
    }

    private getCurrentUsername(): string {
        // Try to get username from localStorage or sessionStorage if available
        // Otherwise use a placeholder - the password manager will use the current URL
        return localStorage.getItem('username') || 'user';
    }

    clearErrors(): void {
        this.errors = [];
    }
}