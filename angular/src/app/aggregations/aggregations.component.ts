import {Component, inject} from '@angular/core';
import {TotalAggregation} from '../aggregations';
import {AggregationsService} from "../aggregations.service";
import {FormsModule} from "@angular/forms";
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';

import {MatSlideToggleModule} from '@angular/material/slide-toggle';

@Component({
    templateUrl: './aggregations.component.html',
    styleUrls: ['./aggregations.component.scss'],
    selector: 'app-aggregations',
    imports: [
    FormsModule,
    MatCardModule,
    MatIconModule,
    MatDividerModule,
    MatSlideToggleModule
]
})
export class AggregationsComponent {
    totalAggregation: TotalAggregation | null = null;
    aggregationService: AggregationsService = inject(AggregationsService);
    isYTD = false;

    constructor() {
        this.refresh();
    }

    refresh() {
        this.aggregationService.getAll('', this.isYTD).subscribe((totalAggregation: TotalAggregation) => {
            this.totalAggregation = totalAggregation;
        });
    }

    getKeys(obj: { [key: string]: any }, ascending: boolean = false): string[] {
        // If ascending is true, sort numerically (for months), else descending numerically (for years)
        return Object.keys(obj).sort((a, b) =>
            ascending
                ? Number(a) - Number(b)
                : Number(b) - Number(a)
        );
    }

    formatDuration(minutes: number): string {
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
    }

    monthName(month: string): string {
        // Handles both "1" and "01" as input
        const monthNum = parseInt(month, 10);
        const monthNames = [
            'January', 'February', 'March', 'April', 'May', 'June',
            'July', 'August', 'September', 'October', 'November', 'December'
        ];
        return monthNames[monthNum - 1] || month;
    }
}