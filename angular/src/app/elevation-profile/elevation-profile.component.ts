import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';

import { BaseChartDirective } from 'ng2-charts';
import {
    Chart, ChartConfiguration, ChartEvent, ActiveElement,
    CategoryScale, LinearScale, PointElement, LineElement, LineController,
    Filler, Tooltip
} from 'chart.js';
import { Waypoint } from '../activity';

Chart.register(CategoryScale, LinearScale, PointElement, LineElement, LineController, Filler, Tooltip);

@Component({
    selector: 'app-elevation-profile',
    imports: [BaseChartDirective],
    templateUrl: './elevation-profile.component.html',
    styleUrls: ['./elevation-profile.component.scss']
})
export class ElevationProfileComponent implements OnChanges {
    @Input() waypoints: Waypoint[] = [];
    @Output() waypointHovered = new EventEmitter<Waypoint | null>();
    @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

    chartData: ChartConfiguration<'line'>['data'] = {
        labels: [],
        datasets: []
    };

    chartOptions: ChartConfiguration<'line'>['options'] = {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
            mode: 'index',
            intersect: false
        },
        plugins: {
            legend: {
                display: true
            },
            tooltip: {
                callbacks: {
                    title: (items) => {
                        if (items.length > 0) {
                            return `${items[0].label} km`;
                        }
                        return '';
                    },
                    label: (item) => {
                        const value = Number(item.parsed.y).toFixed(1);
                        const unit = item.dataset.yAxisID === 'yHeartRate' ? 'bpm'
                            : item.dataset.yAxisID === 'yPower' ? 'W'
                                : 'm';
                        const label = item.dataset.label ?? 'Value';
                        return `${label}: ${value} ${unit}`;
                    }
                }
            }
        },
        scales: {
            x: {
                title: {
                    display: true,
                    text: 'Distance (km)'
                },
                ticks: {
                    maxTicksLimit: 15
                }
            },
            yElevation: {
                type: 'linear',
                position: 'left',
                title: {
                    display: true,
                    text: 'Elevation (m)'
                }
            },
            yHeartRate: {
                type: 'linear',
                position: 'right',
                grid: {
                    drawOnChartArea: false
                },
                title: {
                    display: true,
                    text: 'Heart Rate (bpm)'
                }
            },
            yPower: {
                type: 'linear',
                position: 'right',
                offset: true,
                grid: {
                    drawOnChartArea: false
                },
                title: {
                    display: true,
                    text: 'Power (W)'
                }
            }
        },
        onHover: (_event: ChartEvent, elements: ActiveElement[], chart: Chart) => {
            if (elements.length > 0) {
                const index = elements[0].index;
                const waypoint = this.waypoints[index];
                if (waypoint?.latlon && waypoint.latlon.lat != null && waypoint.latlon.lon != null) {
                    this.waypointHovered.emit(waypoint);
                } else {
                    this.waypointHovered.emit(null);
                }
            } else {
                this.waypointHovered.emit(null);
            }
        }
    };

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['waypoints'] && this.waypoints?.length > 0) {
            this.updateChartData();
        }
    }

    highlightByIndex(index: number): void {
        if (!this.chart?.chart || index < 0 || index >= this.waypoints.length) return;
        const chart = this.chart.chart;
        const active = chart.data.datasets.map((_ds, datasetIndex) => ({ datasetIndex, index }));
        chart.setActiveElements(active);
        chart.tooltip?.setActiveElements(active, { x: 0, y: 0 });
        chart.update('none');
    }

    clearHighlight(): void {
        if (!this.chart?.chart) return;
        this.chart.chart.setActiveElements([]);
        this.chart.chart.tooltip?.setActiveElements([], { x: 0, y: 0 });
        this.chart.chart.update('none');
    }

    private updateChartData(): void {
        const labels = this.waypoints.map(wp => (wp.distanceInMeter / 1000).toFixed(1));
        const altitudes = this.waypoints.map(wp => wp.altitude);
        const heartRates = this.waypoints.map(wp => wp.heartRate);
        const powers = this.waypoints.map(wp => wp.power);

        this.chartData = {
            labels,
            datasets: [
                {
                    label: 'Elevation',
                    yAxisID: 'yElevation',
                    data: altitudes,
                    fill: true,
                    borderColor: '#ff7800',
                    backgroundColor: 'rgba(255, 120, 0, 0.2)',
                    borderWidth: 1.5,
                    pointRadius: 0,
                    pointHitRadius: 5,
                    pointHoverRadius: 4,
                    pointHoverBackgroundColor: '#ff7800',
                    tension: 0.1
                },
                {
                    label: 'Heart Rate',
                    yAxisID: 'yHeartRate',
                    data: heartRates,
                    fill: false,
                    borderColor: '#e53935',
                    borderWidth: 1.5,
                    pointRadius: 0,
                    pointHitRadius: 5,
                    pointHoverRadius: 4,
                    pointHoverBackgroundColor: '#e53935',
                    tension: 0.1
                },
                {
                    label: 'Power',
                    yAxisID: 'yPower',
                    data: powers,
                    fill: false,
                    borderColor: '#1e88e5',
                    borderWidth: 1.5,
                    pointRadius: 0,
                    pointHitRadius: 5,
                    pointHoverRadius: 4,
                    pointHoverBackgroundColor: '#1e88e5',
                    tension: 0.1
                }
            ]
        };
    }
}
