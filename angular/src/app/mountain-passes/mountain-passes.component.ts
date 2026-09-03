import {CommonModule} from '@angular/common';
import {Component, DestroyRef, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {RouterLink} from '@angular/router';
import {AllCrossedPasses, MountainPassService, PassCrossing} from '../mountain-pass.service';

@Component({
    selector: 'app-mountain-passes',
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatCardModule,
        MatIconModule,
        MatProgressSpinnerModule,
        RouterLink
    ],
    templateUrl: './mountain-passes.component.html',
    styleUrls: ['./mountain-passes.component.scss']
})
export class MountainPassesComponent {
    private readonly destroyRef = inject(DestroyRef);
    private readonly mountainPassService = inject(MountainPassService);

    response?: AllCrossedPasses;
    loading = false;
    errorMessage?: string;

    constructor() {
        this.loadPasses();
    }

    reload(): void {
        this.loadPasses();
    }

    private loadPasses(): void {
        this.loading = true;
        this.errorMessage = undefined;
        this.mountainPassService.getAllCrossedPasses()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: response => {
                    this.response = response;
                    this.loading = false;
                },
                error: () => {
                    this.loading = false;
                    this.errorMessage = 'Mountain passes could not be loaded.';
                }
            });
    }

    crossingsCount(): number {
        return this.response?.passCrossings.length ?? 0;
    }

    passLabel(crossing: PassCrossing): string {
        return crossing.passCountry ? `${crossing.passName} (${crossing.passCountry})` : crossing.passName;
    }
}
