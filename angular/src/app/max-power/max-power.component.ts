import {Component, inject} from '@angular/core';
import {ActivatedRoute, RouterModule} from '@angular/router';
import {MaxPowerService} from '../max-power.service';
import {MaxPower} from '../maxPower';
import {CommonModule} from "@angular/common";
import {DurationUtilService} from '../duration-util.service';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@Component({
    selector: 'max-power',
    imports: [
        CommonModule,
        RouterModule,
        MatCardModule,
        MatIconModule,
        MatExpansionModule,
        MatProgressSpinnerModule
    ],
    templateUrl: './max-power.component.html',
    styleUrls: ['./max-power.component.scss']
})
export class MaxPowerComponent {
    maxPower: MaxPower | null = null;
    loading = true;
    maxPowerService: MaxPowerService = inject(MaxPowerService);
    durationUtil: DurationUtilService = inject(DurationUtilService);
    route: ActivatedRoute = inject(ActivatedRoute);

    constructor() {
        const activityId = Number(this.route.snapshot.paramMap.get('id'));
        if (isNaN(activityId) || activityId === 0) {
            this.maxPowerService.getMaxPowerAllActivities().subscribe((maxPower: MaxPower) => {
                this.maxPower = maxPower;
                this.loading = false;
            });
        } else {
            this.maxPowerService.getMaxPowerOfActivity(activityId).subscribe((maxPower: MaxPower) => {
                this.maxPower = maxPower;
                this.loading = false;
            });
        }
    }

    formatDuration(seconds: number): string {
        return this.durationUtil.formatDuration(seconds);
    }
}