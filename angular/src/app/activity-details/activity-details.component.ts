import {Component, DestroyRef, inject, OnInit, ViewChild} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute} from '@angular/router';
import {Activity, Waypoint} from '../activity';
import {ActivitiesService, CsvExportConfigRequest} from '../activities.service';
import {TownFinderService} from "../town-finder.service";
import {MountainPass, MountainPassService} from "../mountain-pass.service";
import {CommonModule} from "@angular/common";
import {FormsModule} from '@angular/forms';
import {LeafletModule} from '@bluehalo/ngx-leaflet';
import {geoJSON, icon, LatLng, latLng, latLngBounds, LeafletMouseEvent, Map, Marker, marker, tileLayer} from 'leaflet';
import {DurationUtilService} from '../duration-util.service';
import {MaxPowerComponent} from '../max-power/max-power.component';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ElevationProfileComponent} from '../elevation-profile/elevation-profile.component';
import {CsvExportConfigDialogComponent} from './csv-export-config-dialog.component';

@Component({
    selector: 'app-activity-details',
    templateUrl: './activity-details.component.html',
    styleUrls: ['./activity-details.component.scss'],
    imports: [
        CommonModule,
        FormsModule,
        LeafletModule,
        MaxPowerComponent,
        MatCardModule,
        MatIconModule,
        MatDividerModule,
        MatDialogModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatSnackBarModule,
        ElevationProfileComponent
    ]
})
export class ActivityDetailsComponent implements OnInit {
    @ViewChild(ElevationProfileComponent) elevationProfile?: ElevationProfileComponent;
    activity: Activity | undefined;
    towns: string[] | undefined;
    boundsTowns: string[] | undefined;
    cornerTowns: string[] | undefined;
    crossedPasses: MountainPass[] | undefined;
    isEditingTitle = false;
    isSavingTitle = false;
    editedTitle = '';
    titleErrorMessage?: string;
    optionsSpec: any = {
        layers: [{url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', attribution: 'Open Street Map'}]
    };
    durationUtil: DurationUtilService = inject(DurationUtilService);
    private readonly destroyRef = inject(DestroyRef);
    private readonly dialog = inject(MatDialog);
    private readonly snackBar = inject(MatSnackBar);

    private readonly defaultCsvExportConfig: CsvExportConfigRequest = {
        latLon: false,
        speed: false,
        power: true,
        heartRate: true,
        cadence: false,
        altitude: false,
        lap: false,
        secondsToAverage: 20
    };

    // Leaflet bindings
    fitBounds = latLngBounds(latLng(0, 0), latLng(0, 0));
    options = {
        layers: [tileLayer(this.optionsSpec.layers[0].url, {attribution: this.optionsSpec.layers[0].attribution})]
    };

    geoJSON = geoJSON(
        ({
            type: 'Polygon',
            coordinates: [[
                [-121.6, 46.87],
                [-121.5, 46.87],
                [-121.5, 46.93],
                [-121.6, 46.87],
                [-121.3, 46.99]
            ]]
        }) as any,
        {style: () => ({color: '#ff7800'})})

    layers = [
        this.geoJSON
    ];

    private leafletMap?: Map;
    private hoverMarker?: Marker;

    onMapReady(map: Map): void {
        this.leafletMap = map;
    }

    onWaypointHovered(waypoint: Waypoint | null): void {
        if (!this.leafletMap) return;

        if (waypoint?.latlon && waypoint.latlon.lat != null && waypoint.latlon.lon != null) {
            const pos = latLng(waypoint.latlon.lat, waypoint.latlon.lon);
            if (this.hoverMarker) {
                this.hoverMarker.setLatLng(pos);
            } else {
                this.hoverMarker = marker(pos, {
                    icon: icon({
                        iconSize: [12, 12],
                        iconAnchor: [6, 6],
                        iconUrl: 'data:image/svg+xml;base64,' + btoa(
                            '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12">' +
                            '<circle cx="6" cy="6" r="5" fill="#ff7800" stroke="white" stroke-width="2"/></svg>'
                        )
                    })
                }).addTo(this.leafletMap);
            }
        } else if (this.hoverMarker) {
            this.hoverMarker.remove();
            this.hoverMarker = undefined;
        }
    }

    constructor(
        private route: ActivatedRoute,
        private activitiesService: ActivitiesService,
        private townFinderService: TownFinderService,
        private mountainPassService: MountainPassService
    ) {
    }

    exportAsCsv(): void {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        this.dialog.open(CsvExportConfigDialogComponent, {
            width: '520px',
            maxWidth: '95vw',
            data: this.defaultCsvExportConfig
        }).afterClosed().subscribe((config: CsvExportConfigRequest | undefined) => {
            if (!config) {
                return;
            }
            this.activitiesService.exportActivityAsCsv(id, config).subscribe((response) => {
                const blob = response.body;
                if (!blob) {
                    throw new Error('CSV export response body is missing.');
                }
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = this.extractDownloadFileName(response.headers.get('content-disposition')) ?? `activity-${id}.csv`;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            });
        });
    }

    private extractDownloadFileName(contentDisposition: string | null): string | undefined {
        if (!contentDisposition) {
            return undefined;
        }
        const match = contentDisposition.match(/filename\*?=(?:UTF-8''|")?([^";]+)"?/i);
        return match?.[1];
    }

    startTitleEdit(): void {
        if (!this.activity || this.isSavingTitle) {
            return;
        }
        this.editedTitle = this.activity.title;
        this.titleErrorMessage = undefined;
        this.isEditingTitle = true;
    }

    cancelTitleEdit(): void {
        this.isEditingTitle = false;
        this.isSavingTitle = false;
        this.titleErrorMessage = undefined;
        this.editedTitle = this.activity?.title ?? '';
    }

    saveTitle(): void {
        if (!this.activity || this.isSavingTitle) {
            return;
        }

        const newTitle = this.editedTitle.trim();
        if (!newTitle) {
            this.titleErrorMessage = 'Title must not be empty.';
            return;
        }

        if (newTitle === this.activity.title) {
            this.cancelTitleEdit();
            return;
        }

        this.isSavingTitle = true;
        this.titleErrorMessage = undefined;
        this.activitiesService.changeTitle(this.activity.id, newTitle)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    if (!this.activity) {
                        return;
                    }
                    this.activity = {...this.activity, title: newTitle};
                    this.isEditingTitle = false;
                    this.isSavingTitle = false;
                    this.editedTitle = newTitle;
                    this.snackBar.open('Title updated.', 'OK', {duration: 3000});
                },
                error: () => {
                    this.isSavingTitle = false;
                    this.titleErrorMessage = 'Title could not be saved.';
                    this.snackBar.open('Title could not be saved.', 'OK', {duration: 5000});
                }
            });
    }

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        this.activitiesService.getActivity(id).subscribe((activity: Activity) => {
            this.activity = activity;
            this.editedTitle = activity.title;
            this.fitBounds = latLngBounds(
                latLng(activity.bounds.south, activity.bounds.west),
                latLng(activity.bounds.north, activity.bounds.east));
        });
        // Load all town types
        this.towns = ["loading..."];
        this.boundsTowns = ["loading..."];
        this.cornerTowns = ["loading..."];

        this.townFinderService.getTownsOfActivity(id).subscribe((towns: string[]) => {
            console.log('Route towns:', towns);
            this.towns = towns;
        });

        this.townFinderService.getTownsOfActivityBounds(id).subscribe((towns: string[]) => {
            console.log('Bounds towns:', towns);
            this.boundsTowns = towns;
        });

        this.townFinderService.getTownsOfActivityCorners(id).subscribe((towns: string[]) => {
            console.log('Corner towns:', towns);
            this.cornerTowns = towns;
        });

        this.mountainPassService.getCrossedPasses(id).subscribe((crossedPasses) => {
            this.crossedPasses = crossedPasses.crossedPasses;
        });

        this.activitiesService.getGeoJSON(id).subscribe((geojson: any) => {
            const routeLayer = geoJSON(geojson,
                {
                    style: () => ({color: '#ff7800'}),
                    coordsToLatLng: (coords: any) => new LatLng(coords[1], coords[0], coords[2])
                }
            );

            // Invisible wider layer for easier mouse interaction
            const hitArea = geoJSON(geojson, {
                style: () => ({color: 'transparent', weight: 30, opacity: 0}),
                coordsToLatLng: (coords: any) => new LatLng(coords[1], coords[0], coords[2])
            });
            hitArea.on('mousemove', (e: LeafletMouseEvent) => {
                this.onRouteHover(e.latlng.lat, e.latlng.lng);
            });
            hitArea.on('mouseout', () => {
                this.elevationProfile?.clearHighlight();
            });

            this.layers.push(routeLayer);
            this.layers.push(hitArea);
        })
    }

    private onRouteHover(lat: number, lon: number): void {
        if (!this.activity?.waypoints?.length || !this.elevationProfile) return;

        let closestIndex = 0;
        let minDist = Infinity;
        for (let i = 0; i < this.activity.waypoints.length; i++) {
            const wp = this.activity.waypoints[i];
            if (wp.latlon && wp.latlon.lat != null && wp.latlon.lon != null) {
                const d = Math.pow(wp.latlon.lat - lat, 2) + Math.pow(wp.latlon.lon - lon, 2);
                if (d < minDist) {
                    minDist = d;
                    closestIndex = i;
                }
            }
        }

        this.elevationProfile.highlightByIndex(closestIndex);
    }
}