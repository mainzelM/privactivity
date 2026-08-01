import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute, Router} from '@angular/router';
import {Activity} from "../activity";
import {ActivityFilterRequest} from "../activity-filter-request";
import {ActivitiesService} from "../activities.service";
import {CommonModule} from "@angular/common";
import {MatTableModule} from '@angular/material/table';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatSortModule} from '@angular/material/sort';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {FormsModule} from '@angular/forms';
import {SelectionModel} from '@angular/cdk/collections';
import {forkJoin, map, switchMap, tap} from 'rxjs';
import {LeafletModule} from '@bluehalo/ngx-leaflet';
import {
    control,
    Control,
    geoJSON,
    icon,
    latLng,
    LatLng,
    LatLngBounds,
    Layer,
    LeafletMouseEvent,
    Map,
    marker,
    tileLayer
} from 'leaflet';

export function createTrackOverlayLabel(color: string, id: number, title: string): string {
    return `<span style="border-left: 4px solid ${color}; padding-left: 6px;"><a href="/activity-details/${id}" class="track-link">${escapeHtml(title)}</a></span>`;
}

function escapeHtml(value: string): string {
    return value
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

@Component({
    selector: 'app-activities',
    imports: [
        CommonModule,
        LeafletModule,
        MatTableModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatSortModule,
        MatProgressSpinnerModule,
        MatCheckboxModule,
        FormsModule
    ],
    templateUrl: './activities.component.html',
    styleUrls: ['./activities.component.scss']
})
export class ActivitiesComponent implements OnInit {
    activities: Activity[] = [];
    loading = false;
    displayedColumns: string[] = ['select', 'id', 'title', 'date', 'distance', 'ascent', 'duration', 'details'];
    selection = new SelectionModel<Activity>(true, []);
    private trackLayers: Layer[] = [];
    private layersControl?: Control.Layers;
    private destroyRef = inject(DestroyRef);
    activityService: ActivitiesService = inject(ActivitiesService);
    router: Router = inject(Router);
    route: ActivatedRoute = inject(ActivatedRoute);

    minDistanceKm: number | null = null;
    maxDistanceKm: number | null = null;
    minAscent: number | null = null;
    maxAscent: number | null = null;
    tileIdFilter: string | null = null;
    titleFilter: string | null = null;

    // Sorting
    sortBy: string = 'date';
    sortDirection: string = 'desc';

    // Location filter
    lat: number | null = null;
    lon: number | null = null;
    radius: number = 5000;
    mapCenter: LatLng = latLng(48.13743, 11.57549);

    // Leaflet
    options = {
        layers: [
            tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 18,
                attribution: 'Open Street Map'
            })
        ],
        zoom: 10,
        center: latLng(48.13743, 11.57549) // Default to Munich or similar central location
    };
    layers: Layer[] = [];
    private leafletMap?: Map;
    private pendingCenter?: LatLng;

    onMapReady(map: Map): void {
        this.leafletMap = map;
        if (this.pendingCenter) {
            map.panTo(this.pendingCenter);
            this.pendingCenter = undefined;
        }
    }

    isAllSelected(): boolean {
        return this.selection.selected.length === this.activities.length && this.activities.length > 0;
    }

    toggleAllRows(): void {
        if (this.isAllSelected()) {
            this.selection.clear();
        } else {
            this.selection.select(...this.activities);
        }
    }

    showSelectedTracks(): void {
        const selected = this.selection.selected;
        if (selected.length === 0) return;

        const requests = selected.map(a => this.activityService.getGeoJSON(a.id));
        forkJoin(requests).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(geoJsons => {
            // Remove previous track layers
            this.clearTrackLayers();

            // Ensure layers control exists
            if (!this.layersControl && this.leafletMap) {
                this.layersControl = control.layers({}, {}, {
                    collapsed: false,
                    position: 'topright'
                }).addTo(this.leafletMap);
            }

            const colors = ['#ff7800', '#1e88e5', '#43a047', '#e53935', '#8e24aa', '#00acc1'];
            const allBounds: LatLngBounds[] = [];

            geoJsons.forEach((geojsonData, i) => {
                const color = colors[i % colors.length];
                const title = selected[i].title || `Track ${selected[i].id}`;
                const id = selected[i].id;
                const layer =
                    geoJSON(geojsonData, {
                        style: () => ({color, weight: 3}),
                        coordsToLatLng: (coords: any) => new LatLng(coords[1], coords[0], coords[2])
                    });
                this.trackLayers.push(layer);
                this.layers.push(layer);
                const bounds = layer.getBounds();
                if (bounds.isValid()) {
                    allBounds.push(bounds);
                }
                this.layersControl?.addOverlay(layer, createTrackOverlayLabel(color, id, title));
            });

            if (allBounds.length > 0 && this.leafletMap) {
                let combined = allBounds[0];
                for (let i = 1; i < allBounds.length; i++) {
                    combined = combined.extend(allBounds[i]);
                }
                this.leafletMap.fitBounds(combined, {padding: [20, 20]});
            }
        });
    }

    clearTrackLayers(): void {
        for (const layer of this.trackLayers) {
            const idx = this.layers.indexOf(layer);
            if (idx >= 0) {
                this.layers.splice(idx, 1);
            }
        }
        this.trackLayers = [];
        if (this.layersControl && this.leafletMap) {
            this.layersControl.remove();
            this.layersControl = undefined;
        }
    }

    ngOnInit(): void {
        this.route.queryParams.pipe(
            map(params => {
                this.minDistanceKm = params['minDistance'] ? +params['minDistance'] : null;
                this.maxDistanceKm = params['maxDistance'] ? +params['maxDistance'] : null;
                this.minAscent = params['minAscent'] ? +params['minAscent'] : null;
                this.maxAscent = params['maxAscent'] ? +params['maxAscent'] : null;
                this.tileIdFilter = params['tileId'] || null;
                this.titleFilter = params['title'] || null;
                this.lat = params['lat'] ? +params['lat'] : null;
                this.lon = params['lon'] ? +params['lon'] : null;
                this.sortBy = params['sortBy'] || 'date';
                this.sortDirection = params['sortDirection'] || 'desc';

                if (this.lat && this.lon) {
                    this.updateMapMarker(this.lat, this.lon);
                    const center = latLng(this.lat, this.lon);
                    this.mapCenter = center;
                    if (this.leafletMap) {
                        this.leafletMap.panTo(center);
                    } else {
                        this.pendingCenter = center;
                    }
                } else {
                    this.layers = [];
                }

                const filter: ActivityFilterRequest = {};
                filter.minDistance = this.minDistanceKm ? this.minDistanceKm * 1000 : undefined;
                filter.maxDistance = this.maxDistanceKm ? this.maxDistanceKm * 1000 : undefined;
                filter.minAscent = this.minAscent ?? undefined;
                filter.maxAscent = this.maxAscent ?? undefined;
                filter.tileId = this.tileIdFilter ?? undefined;
                filter.lat = this.lat ?? undefined;
                filter.lon = this.lon ?? undefined;
                filter.radius = this.radius;
                filter.title = this.titleFilter ?? undefined;
                filter.sortBy = this.sortBy;
                filter.sortDirection = this.sortDirection;

                return filter;
            }),
            tap(() => this.loading = true),
            switchMap(filter => this.activityService.getAllActivities(filter)),
            tap(activities => {
                this.loading = false;
                if (this.lat && this.lon) {
                    // If a location filter is active, don't re-center on the first activity
                    return;
                }
                if (activities && activities.length > 0) {
                    const hasValidBounds = (a: any) => a.bounds && a.bounds.north > a.bounds.south && a.bounds.east > a.bounds.west;
                    const activityWithBounds = activities.find(hasValidBounds);
                    if (activityWithBounds) {
                        const center = latLng(
                            (activityWithBounds.bounds.north + activityWithBounds.bounds.south) / 2,
                            (activityWithBounds.bounds.east + activityWithBounds.bounds.west) / 2);
                        this.mapCenter = center;
                        if (this.leafletMap) {
                            this.leafletMap.panTo(center);
                        } else {
                            this.pendingCenter = center;
                        }
                    }
                }
            })
        ).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(activities => {
            this.activities = activities;
            this.selection.clear();
        });
    }

    onMapClick(event: LeafletMouseEvent): void {
        this.lat = event.latlng.lat;
        this.lon = event.latlng.lng;
        this.updateMapMarker(this.lat!, this.lon!);
        this.applyFilter();
    }

    updateMapMarker(lat: number, lon: number): void {
        this.layers = [
            marker([lat, lon], {
                icon: icon({
                    iconSize: [25, 41],
                    iconAnchor: [13, 41],
                    iconUrl: 'assets/location-pin.svg',
                })
            })
        ];
    }

    applyFilter(): void {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {
                minDistance: this.minDistanceKm || null,
                maxDistance: this.maxDistanceKm || null,
                minAscent: this.minAscent || null,
                maxAscent: this.maxAscent || null,
                tileId: this.tileIdFilter || null,
                title: this.titleFilter || null,
                lat: this.lat || null,
                lon: this.lon || null,
                sortBy: this.sortBy,
                sortDirection: this.sortDirection
            },
            queryParamsHandling: 'merge'
        });
    }

    onSortChange(sortBy: string): void {
        if (this.sortBy === sortBy) {
            // Toggle direction if clicking the same column
            this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            // New column, default to descending for dates, ascending for others
            this.sortBy = sortBy;
            this.sortDirection = sortBy === 'date' ? 'desc' : 'asc';
        }
        this.applyFilter();
    }

    resetFilters(): void {
        this.minDistanceKm = null;
        this.maxDistanceKm = null;
        this.minAscent = null;
        this.maxAscent = null;
        this.tileIdFilter = null;
        this.titleFilter = null;
        this.lat = null;
        this.lon = null;
        this.layers = [];
        this.sortBy = 'date';
        this.sortDirection = 'desc';

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {
                minDistance: null,
                maxDistance: null,
                minAscent: null,
                maxAscent: null,
                tileId: null,
                title: null,
                lat: null,
                lon: null,
                sortBy: 'date',
                sortDirection: 'desc'
            },
            queryParamsHandling: 'merge'
        });
    }

    onRowClicked(activity: Activity): void {
        this.router.navigate(['/activity-details', activity.id]);
    }

    formatDuration(seconds: number): string {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
    }
}