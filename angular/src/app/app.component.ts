import {Component, inject} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {Router, RouterLink, RouterOutlet} from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';

import {AuthService} from "./auth/auth.service";
import {MatIconButton} from "@angular/material/button";

@Component({
    selector: 'app-root',
    imports: [RouterLink, RouterOutlet, MatCardModule, MatIconModule, MatTooltipModule, MatIconButton],
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: true
})
export class AppComponent {
    title = 'activities';
    private authService = inject(AuthService);
    private router = inject(Router);

    readonly loggedIn = toSignal(this.authService.isLoggedIn(), {initialValue: false});
    readonly adminAccess = toSignal(this.authService.isUserInRole('ROLE_ADMIN'), {initialValue: false});

    logout() {
        this.authService.logout();

        this.router.navigateByUrl("/");
    }
}