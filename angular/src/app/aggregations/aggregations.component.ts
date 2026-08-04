import {Component, inject} from '@angular/core';
import {MovingSum, TotalAggregation} from '../aggregations';
import {AggregationsService} from "../aggregations.service";
import {FormsModule} from "@angular/forms";
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {BaseChartDirective} from "ng2-charts";
import {
    CategoryScale,
    Chart,
    ChartConfiguration,
    LinearScale,
    LineController,
    LineElement,
    PointElement,
    ScriptableScaleContext,
    Tooltip
} from "chart.js";
import {forkJoin} from "rxjs";

Chart.register(CategoryScale, LinearScale, PointElement, LineElement, LineController, Tooltip);

@Component({
    templateUrl: './aggregations.component.html',
    styleUrls: ['./aggregations.component.scss'],
    selector: 'app-aggregations',
    imports: [
        FormsModule,
        MatCardModule,
        MatIconModule,
        MatDividerModule,
        MatSlideToggleModule,
        MatTabsModule,
        BaseChartDirective
    ]
})
export class AggregationsComponent {
    totalAggregation: TotalAggregation | null = null;
    aggregationService: AggregationsService = inject(AggregationsService);
    isYTD = false;
    movingSumNumDays = 365;

    movingSumChartData: ChartConfiguration<'line'>['data'] = {
        labels: [],
        datasets: []
    };

    movingSumChartOptions: ChartConfiguration<'line'>['options'] = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: true
            },
            tooltip: {
                callbacks: {
                    title: (items) => items.length > 0 ? String(items[0].label) : '',
                    label: (item) => `Distance: ${Number(item.parsed.y).toFixed(1)} km`
                }
            }
        },
        scales: {
            x: {
                title: {
                    display: true,
                    text: 'Date'
                },
                grid: {
                    color: (context: ScriptableScaleContext) =>
                        this.isYearStartLabel(this.labelAtIndex(context.index))
                            ? 'rgba(30, 136, 229, 0.35)'
                            : 'rgba(0, 0, 0, 0)',
                    lineWidth: (context: ScriptableScaleContext) =>
                        this.isYearStartLabel(this.labelAtIndex(context.index)) ? 1.5 : 1
                },
                ticks: {
                    autoSkip: false,
                    maxRotation: 0,
                    callback: (_value, index) => {
                        const label = this.labelAtIndex(index);
                        return this.isYearStartLabel(label) ? label.substring(0, 4) : '';
                    }
                }
            },
            y: {
                title: {
                    display: true,
                    text: 'Distance (km)'
                }
            }
        }
    };

    constructor() {
        this.refresh();
    }

    refresh() {
        forkJoin({
            totalAggregation: this.aggregationService.getAll('', this.isYTD),
            movingSum: this.aggregationService.getMovingSum(this.movingSumNumDays)
        }).subscribe(({totalAggregation, movingSum}) => {
            this.totalAggregation = totalAggregation;
            this.updateMovingSumChart(movingSum);
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

    private updateMovingSumChart(movingSum: MovingSum): void {
        const entries = Object.entries(movingSum).sort(([dateA], [dateB]) => dateA.localeCompare(dateB));
        const labels = entries.map(([date]) => date);
        const valuesInKm = entries.map(([, meters]) => Math.round((meters / 1000) * 10) / 10);

        this.movingSumChartData = {
            labels,
            datasets: [
                {
                    label: `${this.movingSumNumDays}-day moving sum`,
                    data: valuesInKm,
                    borderColor: 'rgba(30 ,136, 229, 0.8)',
                    backgroundColor: 'rgba(30 ,136, 229, 0.3)',
                    borderWidth: 2,
                    pointRadius: 0,
                    tension: 0.15,
                    fill: true
                }
            ]
        };
    }

    private labelAtIndex(index: number): string {
        const labels = this.movingSumChartData.labels ?? [];
        if (index < 0 || index >= labels.length) {
            return '';
        }
        const label = labels[index];
        return typeof label === 'string' ? label : '';
    }

    private isYearStartLabel(label: string): boolean {
        return /^\d{4}-01-01$/.test(label);
    }
}