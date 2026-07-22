import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, NgZone } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import { layerGroup, latLng, latLngBounds, Layer, Map, rectangle, tileLayer } from 'leaflet';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TileLegend, TileMetric, TilesResponse, TileSummary } from '../tiles';
import { TilesService } from '../tiles.service';

@Component({
    selector: 'app-tiles',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        LeafletModule,
        MatButtonModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        RouterLink
    ],
    templateUrl: './tiles.component.html',
    styleUrls: ['./tiles.component.scss']
})
export class TilesComponent {
    private destroyRef = inject(DestroyRef);
    private tilesService = inject(TilesService);
    private zone = inject(NgZone);

    readonly metrics: Array<{ value: TileMetric; label: string }> = [
        { value: 'activityCount', label: 'Activities' },
        { value: 'maxPower', label: 'Max Power' },
        { value: 'maxHeartRate', label: 'Max Heart Rate' },
        { value: 'averagePower', label: 'Average Power' },
        { value: 'averageHeartRate', label: 'Average Heart Rate' }
    ];

    metric: TileMetric = 'activityCount';
    response?: TilesResponse;
    selectedTile?: TileSummary;
    loading = false;
    errorMessage?: string;

    options = {
        layers: [
            tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 18, attribution: 'Open Street Map' })
        ],
        zoom: 10,
        center: latLng(48.13743, 11.57549)
    };
    layers: Layer[] = [];
    private map?: Map;
    private currentZoom = this.options.zoom;

    private static readonly TEMPERATURE_COLORS = [
        '#2b3f8f', '#2553a0', '#1f67b0', '#197bc0', '#138fd1',
        '#0da3e1', '#16b8dc', '#28c8c4', '#3ad6ab', '#5be08f',
        '#84e56f', '#afe85b', '#d7e955', '#efe04f', '#f7c949',
        '#f8ae43', '#f88f3e', '#f66f39', '#f14f35', '#e63b2f'
    ];

    constructor() {
        this.loadTiles();
    }

    onMapReady(map: Map): void {
        this.map = map;
        this.currentZoom = map.getZoom();
        this.map.on('zoomend', () => {
            if (!this.map) {
                return;
            }
            this.zone.run(() => {
                this.currentZoom = this.map!.getZoom();
                this.refreshLayers();
            });
        });
        this.fitToTiles();
    }

    onMetricChange(): void {
        this.loadTiles(false);
    }

    reloadTiles(): void {
        this.loadTiles();
    }

    private loadTiles(autoFit = true): void {
        this.loading = true;
        this.errorMessage = undefined;
        this.tilesService.getTiles(this.metric)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: response => this.applyResponse(response, autoFit),
                error: () => {
                    this.loading = false;
                    this.errorMessage = 'Tiles could not be loaded.';
                    this.layers = [];
                }
            });
    }

    private applyResponse(response: TilesResponse, autoFit = true): void {
        this.response = response;
        this.selectedTile = response.tiles[0];
        this.refreshLayers();
        this.loading = false;
        if (autoFit) {
            this.fitToTiles();
        }
    }

    private refreshLayers(): void {
        if (!this.response) {
            this.layers = [];
            return;
        }
        this.layers = this.createTileLayers(this.response.tiles, this.response.legend);
    }

    private createTileLayers(tiles: TileSummary[], legend: TileLegend): Layer[] {
        return tiles.map(tile => {
            const layer = rectangle([
                [tile.bounds.south, tile.bounds.west],
                [tile.bounds.north, tile.bounds.east]
            ], {
                color: this.borderColor(tile, legend),
                opacity: this.selectedTile?.tileId === tile.tileId ? 0.9 : 0.6,
                weight: this.borderWeight(this.selectedTile?.tileId === tile.tileId),
                fill: true,
                fillOpacity: 0,
                fillColor: 'transparent'
            });

            layer.bindTooltip(this.tooltipFor(tile), { sticky: true });
            layer.on('click', () => {
                this.zone.run(() => {
                    this.selectedTile = tile;
                    this.refreshLayers();
                });
            });
            return layer;
        });
    }

    private borderWeight(selected: boolean): number {
        const normalizedZoom = this.normalizeZoom(this.currentZoom);
        const baseWeight = 2 + (normalizedZoom * 3);
        return selected ? baseWeight + 2 : baseWeight;
    }

    private normalizeZoom(zoom: number): number {
        const minZoom = 6;
        const maxZoom = 16;
        if (zoom <= minZoom) {
            return 0;
        }
        if (zoom >= maxZoom) {
            return 1;
        }
        return (zoom - minZoom) / (maxZoom - minZoom);
    }

    private fitToTiles(): void {
        if (!this.map || !this.response || this.response.tiles.length === 0) {
            return;
        }

        const bounds = latLngBounds(this.response.tiles.flatMap(tile => [
            [tile.bounds.south, tile.bounds.west],
            [tile.bounds.north, tile.bounds.east]
        ] as [number, number][]));
        this.map.fitBounds(bounds, { padding: [20, 20] });
    }

    private tooltipFor(tile: TileSummary): string {
        const value = this.metricValue(tile);
        return `${tile.activityIds.length} Activities${value !== null ? ` • ${this.metricLabel()}: ${value.toFixed(1)}` : ''}`;
    }

    private borderColor(tile: TileSummary, legend: TileLegend): string {
        const value = this.metricValue(tile);
        if (value === null || legend.quantiles.length === 0) {
            return '#9fb3bf';
        }

        const thresholdIndex = legend.quantiles.findIndex(quantile => value <= quantile);
        if (thresholdIndex === -1) {
            return TilesComponent.TEMPERATURE_COLORS[TilesComponent.TEMPERATURE_COLORS.length - 1];
        }
        return TilesComponent.TEMPERATURE_COLORS[thresholdIndex];
    }

    quantileLabel(legend: TileLegend, index: number): number | string {
        return legend.quantiles.length > index ? legend.quantiles[index] : 'n/a';
    }

    metricValue(tile: TileSummary): number | null {
        switch (this.metric) {
            case 'activityCount':
                return tile.activityIds.length;
            case 'maxPower':
                return tile.maxPower;
            case 'maxHeartRate':
                return tile.maxHeartRate;
            case 'averagePower':
                return tile.averagePower;
            case 'averageHeartRate':
                return tile.averageHeartRate;
        }
    }

    metricLabel(): string {
        return this.metrics.find(metric => metric.value === this.metric)?.label ?? this.metric;
    }
}