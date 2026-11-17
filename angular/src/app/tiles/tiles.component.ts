import {CommonModule} from '@angular/common';
import {Component, DestroyRef, inject, NgZone} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {LeafletModule} from '@bluehalo/ngx-leaflet';
import {latLng, latLngBounds, Layer, Map, tileLayer} from 'leaflet';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSelectModule} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {TILE_METRICS, TileLegend, TileMetric, TilesResponse, TileSummary} from '../tiles';
import {TilesService} from '../tiles.service';
import {createTileRectangle, tileMetricLabel, tileMetricValue, tileOverlayStyle} from './tile-overlay';

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

    readonly metrics = TILE_METRICS;

    metric: TileMetric = 'activityCount';
    response?: TilesResponse;
    selectedTile?: TileSummary;
    loading = false;
    errorMessage?: string;

    options = {
        layers: [
            tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 18,
                attribution: 'Open Street Map',
                referrerPolicy: 'origin-when-cross-origin'
            })
        ],
        zoom: 10,
        center: latLng(48.13743, 11.57549)
    };
    layers: Layer[] = [];
    private map?: Map;
    private currentZoom = this.options.zoom;

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
            const selected = this.selectedTile?.tileId === tile.tileId;
            const layer = createTileRectangle(tile, this.metric,
                tileOverlayStyle(tile, legend, this.metric, this.currentZoom, selected));

            layer.on('click', () => {
                this.zone.run(() => {
                    this.selectedTile = tile;
                    this.refreshLayers();
                });
            });
            return layer;
        });
    }

    private fitToTiles(): void {
        if (!this.map || !this.response || this.response.tiles.length === 0) {
            return;
        }

        const bounds = latLngBounds(this.response.tiles.flatMap(tile => [
            [tile.bounds.south, tile.bounds.west],
            [tile.bounds.north, tile.bounds.east]
        ] as [number, number][]));
        this.map.fitBounds(bounds, {padding: [20, 20]});
    }

    quantileLabel(legend: TileLegend, index: number): number | string {
        return legend.quantiles.length > index ? legend.quantiles[index] : 'n/a';
    }

    metricValue(tile: TileSummary): number | null {
        return tileMetricValue(tile, this.metric);
    }

    metricLabel(): string {
        return tileMetricLabel(this.metric);
    }
}