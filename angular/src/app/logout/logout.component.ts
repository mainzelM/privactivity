import { Component } from '@angular/core';
import { AuthService } from "../auth/auth.service";
import { Router } from "@angular/router";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'app-logout',
    standalone: true,
    imports: [MatButtonModule, MatCardModule, MatIconModule],
    templateUrl: './logout.component.html',
    styleUrls: ['./logout.component.css']
})
export class LogoutComponent {
    constructor(private authService: AuthService, private router: Router, private snackBar: MatSnackBar) {
    }

    public logout(): void {
        this.authService.logout();
        this.preventSilentSignIn();
        this.snackBar.open(`Logout successful`, "OK", { duration: 5000 });
        this.router.navigateByUrl("/login");
    }

    private preventSilentSignIn(): void {
        if (!navigator.credentials?.preventSilentAccess) {
            return;
        }

        navigator.credentials.preventSilentAccess().catch(() => {
            // Ignore failures; logout must continue.
        });
    }
}
