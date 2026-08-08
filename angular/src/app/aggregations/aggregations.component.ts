import {Component, inject} from '@angular/core';
import {EddigtionChart, MovingSum, TotalAggregation} from '../aggregations';
import {AggregationsService} from "../aggregations.service";
import {FormsModule} from "@angular/forms";
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {MatTabsModule} from '@angular/material/tabs';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {BaseChartDirective} from "ng2-charts";
import {
    BarController,
    BarElement,
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

Chart.register(CategoryScale, LinearScale, PointElement, LineElement, LineController, BarController, BarElement, Tooltip);

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
    eddigtionChart: EddigtionChart | null = null;

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

    eddingtonChartData: ChartConfiguration<'bar'>['data'] = {
        labels: [],
        datasets: []
    };

    eddingtonChartOptions: ChartConfiguration<'bar'>['options'] = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: false
            },
            tooltip: {
                callbacks: {
                    title: (items) => items.length > 0 ? `>= ${items[0].label} km` : '',
                    label: (item) => {
                        const idx = (item as any).dataIndex;
                        const orig = this.eddigtionChart?.countsPerKM?.[idx];
                        return orig !== undefined ? `${orig} activities` : `${item.parsed.y} activities`;
                    }
                }
            }
        },
        scales: {
            x: {
                title: {
                    display: true,
                    text: 'Distance threshold (km)'
                }
            },
            y: {
                beginAtZero: true,
                title: {
                    display: true,
                    text: 'Number of activities'
                },
                ticks: {
                    callback: (value) => {
                        const v = Number(value);
                        const original = Math.round(Math.pow(v, 2));
                        return String(original);
                    }
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
            movingSum: this.aggregationService.getMovingSum(this.movingSumNumDays),
            eddingtonChart: this.aggregationService.getEddingtonChart()
        }).subscribe(({totalAggregation, movingSum, eddingtonChart}) => {
            this.totalAggregation = totalAggregation;
            this.updateMovingSumChart(movingSum);
            this.eddigtionChart = eddingtonChart;
            this.updateEddingtonChart(eddingtonChart);
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

    private updateEddingtonChart(eddingtonChart: EddigtionChart): void {
        const labels = eddingtonChart.countsPerKM.map((_count, index) => String(index));
        // transform values with sqrt to reduce dynamic range while preserving order
        const transformed = eddingtonChart.countsPerKM.map(c => Math.sqrt(c));
        const maxOriginal = eddingtonChart.countsPerKM.length > 0 ? Math.max(...eddingtonChart.countsPerKM) : 1;
        const maxTransformed = Math.sqrt(Math.max(maxOriginal, 1));

        this.eddingtonChartData = {
            labels,
            datasets: [
                {
                    data: transformed,
                    backgroundColor: labels.map((label) =>
                        Number(label) === eddingtonChart.eddigtionNumber
                            ? 'rgba(255, 152, 0, 0.85)'
                            : 'rgba(30, 136, 229, 0.75)'
                    ),
                    borderColor: labels.map((label) =>
                        Number(label) === eddingtonChart.eddigtionNumber
                            ? 'rgba(255, 152, 0, 1)'
                            : 'rgba(30, 136, 229, 1)'
                    ),
                    borderWidth: 1
                }
            ]
        };

        // adjust y axis maximum so small values remain visible relative to max
        try {
            const scales: any = (this.eddingtonChartOptions as any).scales || {};
            scales.y = scales.y || {};
            scales.y.max = maxTransformed;
            (this.eddingtonChartOptions as any).scales = scales;
        } catch (e) {
            // ignore if mutation not allowed
        }
    }
}