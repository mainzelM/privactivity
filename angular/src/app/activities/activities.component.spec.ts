import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting, TestRequest} from '@angular/common/http/testing';
import {provideLocationMocks} from '@angular/common/testing';
import {NavigationEnd, provideRouter, Router} from '@angular/router';
import {TestbedHarnessEnvironment} from '@angular/cdk/testing/testbed';
import {MatFormFieldHarness} from '@angular/material/form-field/testing';
import {MatSelectHarness} from '@angular/material/select/testing';
import {MatSlideToggleHarness} from '@angular/material/slide-toggle/testing';
import {LeafletDirective} from '@bluehalo/ngx-leaflet';
import {latLng, latLngBounds, Map as LeafletMap, Rectangle} from 'leaflet';
import {filter, firstValueFrom, of} from 'rxjs';

import {ActivitiesComponent, createTrackOverlayLabel} from './activities.component';
import {Activity} from '../activity';
import {TileLegend, TileMetric, TilesResponse, TileSummary} from '../tiles';
import {TilesService} from '../tiles.service';
import {tileOverlayStyle, tileTooltip} from '../tiles/tile-overlay';
import {environment} from '../../environments/environment';

describe('createTrackOverlayLabel', () => {
    it('escapes the activity title before embedding it in HTML', () => {
        const label = createTrackOverlayLabel('#ff7800', 42, `Run <img src=x onerror="alert(1)"> & "friends"`);
        const parser = new DOMParser();
        const document = parser.parseFromString(label, 'text/html');

        expect(label).toContain('href="/activity-details/42"');
        expect(label).toContain('Run &lt;img src=x onerror=&quot;alert(1)&quot;&gt; &amp; &quot;friends&quot;');
        expect(document.querySelector('img')).toBeNull();
        expect(document.querySelector('a')?.textContent).toBe(`Run <img src=x onerror="alert(1)"> & "friends"`);
    });
});

const TILES_URL = `${environment.apiUrl}/tiles`;
const ACTIVITIES_URL = `${environment.apiUrl}/activities`;

function tile(tileId: string, west: number, overrides: Partial<TileSummary> = {}): TileSummary {
    return {
        tileId,
        bounds: {south: 48.0, north: 48.005, west, east: west + 0.01},
        firstActivityId: 1,
        latestActivityId: 2,
        maxPower: 250,
        maxHeartRate: 170,
        averagePower: 180,
        averageHeartRate: 140,
        activityIds: [1, 2],
        ...overrides
    };
}

// The server serializes the Java enum constant, not the camelCase name that is used as the request parameter.
const SERVER_METRIC: Record<TileMetric, string> = {
    activityCount: 'ACTIVITY_COUNT',
    maxPower: 'MAX_POWER',
    maxHeartRate: 'MAX_HEART_RATE',
    averagePower: 'AVERAGE_POWER',
    averageHeartRate: 'AVERAGE_HEART_RATE'
};

function tilesResponse(metric: TileMetric, tiles: TileSummary[]): TilesResponse {
    const serverMetric = SERVER_METRIC[metric];
    return {
        metric: serverMetric,
        legend: {metric: serverMetric, min: 1, max: 20, quantiles: Array.from({length: 20}, (_, index) => index + 1)},
        tileWidthDegrees: 0.01,
        tileHeightDegrees: 0.005,
        generatedAt: null,
        tiles
    };
}

const TILE_A = tile('12:5', 11.50);
const TILE_B = tile('13:5', 11.51, {maxPower: 300, activityIds: [3]});

const ACTIVITY: Activity = {
    id: 1,
    title: 'Morning ride',
    start: '2025-05-01T06:30:00Z',
    dayOfWeek: 'THURSDAY',
    waypoints: [],
    totals: {distance: 42000, duration: 5400, ascent: 350, descent: 350},
    averages: {speedInMetersPerHour: 28000, cadence: 85, heartRate: 140, power: 180},
    maxima: {speedInMetersPerHour: 50000, heartRate: 170, power: 250, altitude: 600},
    bounds: {south: 0, north: 0, west: 0, east: 0},
    summary: {
        trainingStressScore: null,
        totalTrainingEffect: null,
        intensityFactor: null,
        avgLeftPco: null,
        avgRightPco: null
    }
};

describe('ActivitiesComponent tiles', () => {
    let fixture: ComponentFixture<ActivitiesComponent>;
    let testee: ActivitiesComponent;
    let http: HttpTestingController;
    let router: Router;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [ActivitiesComponent],
            providers: [provideRouter([]), provideLocationMocks(), provideHttpClient(), provideHttpClientTesting()]
        });
    });

    afterEach(() => http?.verify());

    function createTestee(): void {
        http = TestBed.inject(HttpTestingController);
        router = TestBed.inject(Router);
        fixture = TestBed.createComponent(ActivitiesComponent);
        testee = fixture.componentInstance;
        // Without the OpenStreetMap base layer the test stays offline.
        testee.options = {layers: [], zoom: 10, center: latLng(48.0, 11.5)};
        fixture.detectChanges();
    }

    function openPage(tiles: TileSummary[] = [TILE_A, TILE_B]): void {
        createTestee();
        testee.onShowTilesChange(true);
        expectTilesRequest('activityCount').flush(tilesResponse('activityCount', tiles));
        expectActivitiesRequest().flush([]);
    }

    function expectTilesRequest(metric: TileMetric): TestRequest {
        return http.expectOne(request => request.url === TILES_URL && request.params.get('metric') === metric);
    }

    function expectActivitiesRequest(tileId: string | null = null): TestRequest {
        return http.expectOne(request => request.url === ACTIVITIES_URL && request.params.get('tileId') === tileId);
    }

    function leafletMap(): LeafletMap {
        return fixture.debugElement.query(By.directive(LeafletDirective)).injector.get(LeafletDirective).getMap();
    }

    function overlayRectangles(): Rectangle[] {
        const rectangles: Rectangle[] = [];
        leafletMap().eachLayer(layer => {
            if (layer instanceof Rectangle) {
                rectangles.push(layer);
            }
        });
        return rectangles;
    }

    function rectangleOf(tile: TileSummary): Rectangle {
        const bounds = latLngBounds([tile.bounds.south, tile.bounds.west], [tile.bounds.north, tile.bounds.east]);
        const rectangle = overlayRectangles().find(candidate => candidate.getBounds().equals(bounds));
        if (!rectangle) {
            throw new Error(`The map has no rectangle for tile ${tile.tileId}`);
        }
        return rectangle;
    }

    function expectTileStyle(tile: TileSummary, selected: boolean, metric: TileMetric = 'activityCount',
                             legend: TileLegend = tilesResponse(metric, []).legend): void {
        const expected = tileOverlayStyle(tile, legend, metric, leafletMap().getZoom(), selected);
        const {color, weight, opacity} = rectangleOf(tile).options;

        expect({color, weight, opacity}).toEqual({color: expected.color, weight: expected.weight, opacity: expected.opacity});
    }

    async function navigation(action: () => void): Promise<void> {
        const navigated = firstValueFrom(router.events.pipe(filter(event => event instanceof NavigationEnd)));
        action();
        await navigated;
    }

    async function tilesToggle(): Promise<MatSlideToggleHarness> {
        return TestbedHarnessEnvironment.loader(fixture).getHarness(MatSlideToggleHarness.with({label: 'Tiles'}));
    }

    async function tileMetricSelect(): Promise<MatSelectHarness> {
        const field = await TestbedHarnessEnvironment.loader(fixture)
            .getHarness(MatFormFieldHarness.with({floatingLabelText: 'Tile metric'}));
        return (await field.getControl(MatSelectHarness))!;
    }

    function loadingSpinner(): Element | null {
        return fixture.nativeElement.querySelector('[aria-label="Loading tiles"]');
    }

    it('does not request tiles until they are switched on', async () => {
        createTestee();
        expectActivitiesRequest().flush([]);
        const toggle = await tilesToggle();

        expect(testee.showTiles).toBeFalse();
        expect(await toggle.isChecked()).toBeFalse();
        http.expectNone(request => request.url === TILES_URL);

        await toggle.check();
        expectTilesRequest('activityCount').flush(tilesResponse('activityCount', [TILE_A]));

        expect(overlayRectangles().length).toBe(1);
    });

    it('draws one rectangle per tile in a pane between the base map and the tracks', () => {
        openPage();

        const pane = leafletMap().getPane('tileOverlay')!;
        expect(overlayRectangles().length).toBe(2);
        for (const rectangle of overlayRectangles()) {
            expect(pane.contains(rectangle.getElement()!)).toBeTrue();
        }
        // Above Leaflet's tile pane (200), below its overlay pane (400) that holds the tracks.
        expect(Number(pane.style.zIndex)).toBeGreaterThan(200);
        expect(Number(pane.style.zIndex)).toBeLessThan(400);
    });

    it('styles and labels the tiles for the activity count by default', () => {
        openPage();

        expectTileStyle(TILE_A, false);
        expectTileStyle(TILE_B, false);
        expect(rectangleOf(TILE_A).getTooltip()?.getContent()).toBe(tileTooltip(TILE_A, 'activityCount'));
    });

    it('draws tiles that are available before the map is ready', () => {
        TestBed.overrideProvider(TilesService, {
            useValue: {getTiles: () => of(tilesResponse('activityCount', [TILE_A, TILE_B]))}
        });

        createTestee();
        testee.onShowTilesChange(true);
        expectActivitiesRequest().flush([]);

        expect(overlayRectangles().length).toBe(2);
        expectTileStyle(TILE_A, false);
    });

    it('adapts the border weight of the tiles to the zoom level', () => {
        openPage();
        const weightBefore = rectangleOf(TILE_A).options.weight!;

        leafletMap().setZoom(14, {animate: false});

        expectTileStyle(TILE_A, false);
        expect(rectangleOf(TILE_A).options.weight).toBeGreaterThan(weightBefore);
    });

    it('shows a spinner while the tiles are loading', () => {
        createTestee();
        expectActivitiesRequest().flush([]);
        testee.onShowTilesChange(true);
        const tilesRequest = expectTilesRequest('activityCount');

        fixture.detectChanges();
        expect(loadingSpinner()).not.toBeNull();

        tilesRequest.flush(tilesResponse('activityCount', [TILE_A]));
        fixture.detectChanges();
        expect(loadingSpinner()).toBeNull();
    });

    it('says so if there are no tiles yet', () => {
        openPage([]);

        fixture.detectChanges();

        expect(fixture.nativeElement.textContent).toContain('No tiles available yet.');
        expect(overlayRectangles()).toEqual([]);
    });

    it('shows an error if the tiles fail to load but still lists the activities', () => {
        createTestee();
        testee.onShowTilesChange(true);
        expectTilesRequest('activityCount').flush('boom', {status: 500, statusText: 'Server Error'});
        expectActivitiesRequest().flush([ACTIVITY]);

        fixture.detectChanges();

        expect(fixture.nativeElement.querySelector('.tile-status-error')?.textContent).toContain('Tiles could not be loaded.');
        expect(overlayRectangles()).toEqual([]);
        expect(testee.activities).toEqual([ACTIVITY]);
    });

    it('reloads and restyles the tiles for the metric picked in the select', async () => {
        openPage();
        const select = await tileMetricSelect();

        await select.open();
        await select.clickOptions({text: 'Max Power'});

        expectTilesRequest('maxPower').flush(tilesResponse('maxPower', [TILE_A, TILE_B]));
        expect(overlayRectangles().length).toBe(2);
        expectTileStyle(TILE_B, false, 'maxPower');
        expect(rectangleOf(TILE_B).getTooltip()?.getContent()).toBe(tileTooltip(TILE_B, 'maxPower'));
    });

    it('cancels the pending tile request when another metric is picked', () => {
        openPage();

        testee.tileMetric = 'maxPower';
        testee.onTileMetricChange();
        const superseded = expectTilesRequest('maxPower');
        testee.tileMetric = 'averagePower';
        testee.onTileMetricChange();
        expectTilesRequest('averagePower').flush(tilesResponse('averagePower', [TILE_A]));

        expect(superseded.cancelled).toBeTrue();
        expect(overlayRectangles().length).toBe(1);
        expectTileStyle(TILE_A, false, 'averagePower');
    });

    it('keeps styling the loaded tiles for their own metric while another metric is still loading', () => {
        openPage();

        testee.tileMetric = 'maxPower';
        testee.onTileMetricChange();
        const pending = expectTilesRequest('maxPower');
        leafletMap().setZoom(14, {animate: false});

        expectTileStyle(TILE_A, false, 'activityCount');
        expect(rectangleOf(TILE_A).getTooltip()?.getContent()).toBe(tileTooltip(TILE_A, 'activityCount'));
        expect(pending.cancelled).toBeFalse();
    });

    it('draws the tiles for a metric none of the activities has data for, as the server reports it', () => {
        const withoutPower = tile('14:5', 11.52, {
            maxPower: 0, maxHeartRate: 0, averagePower: null, averageHeartRate: null, activityIds: [4]
        });
        const noDataLegend: TileLegend = {metric: 'MAX_POWER', min: null, max: null, quantiles: []};
        openPage();

        testee.tileMetric = 'maxPower';
        testee.onTileMetricChange();
        expectTilesRequest('maxPower').flush({...tilesResponse('maxPower', [withoutPower]), legend: noDataLegend});

        expect(overlayRectangles().length).toBe(1);
        expectTileStyle(withoutPower, false, 'maxPower', noDataLegend);
        expect(rectangleOf(withoutPower).getTooltip()?.getContent()).toBe(tileTooltip(withoutPower, 'maxPower'));
    });

    it('removes the tiles from the map when switched off and restores them without a new request', async () => {
        openPage();
        const toggle = await tilesToggle();
        const select = await tileMetricSelect();

        await toggle.uncheck();

        expect(overlayRectangles()).toEqual([]);
        expect(await select.isDisabled()).toBeTrue();

        await toggle.check();

        expect(overlayRectangles().length).toBe(2);
        expect(await select.isDisabled()).toBeFalse();
        http.expectNone(request => request.url === TILES_URL);
    });

    it('retries a failed tile request when the tiles are switched back on', async () => {
        createTestee();
        testee.onShowTilesChange(true);
        expectTilesRequest('activityCount').flush('boom', {status: 500, statusText: 'Server Error'});
        expectActivitiesRequest().flush([]);
        const toggle = await tilesToggle();

        await toggle.uncheck();
        await toggle.check();

        expectTilesRequest('activityCount').flush(tilesResponse('activityCount', [TILE_A]));
        expect(testee.tilesError).toBeUndefined();
        expect(overlayRectangles().length).toBe(1);
    });

    it('filters by the clicked tile without also picking a location', async () => {
        openPage();

        await navigation(() => rectangleOf(TILE_B).getElement()!.dispatchEvent(new MouseEvent('click', {bubbles: true})));

        expect(router.parseUrl(router.url).queryParams)
            .toEqual({tileId: TILE_B.tileId, sortBy: 'date', sortDirection: 'desc'});
        expectActivitiesRequest(TILE_B.tileId).flush([ACTIVITY]);
        expect(testee.activities).toEqual([ACTIVITY]);
        expect(testee.layers).toEqual([]);
    });

    it('highlights the tile of the applied tile filter until the filters are reset', async () => {
        openPage();

        await router.navigate([], {queryParams: {tileId: TILE_A.tileId}});
        expectActivitiesRequest(TILE_A.tileId).flush([]);

        expectTileStyle(TILE_A, true);
        expectTileStyle(TILE_B, false);

        await navigation(() => testee.resetFilters());
        expectActivitiesRequest().flush([]);

        expectTileStyle(TILE_A, false);
    });

    it('keeps the selected tile highlighted when the tiles are reloaded', async () => {
        openPage();
        await router.navigate([], {queryParams: {tileId: TILE_B.tileId}});
        expectActivitiesRequest(TILE_B.tileId).flush([]);

        testee.tileMetric = 'maxPower';
        testee.onTileMetricChange();
        expectTilesRequest('maxPower').flush(tilesResponse('maxPower', [TILE_A, TILE_B]));

        expectTileStyle(TILE_B, true, 'maxPower');
        expectTileStyle(TILE_A, false, 'maxPower');
    });

    it('does not highlight a tile id that is typed but not applied yet', () => {
        openPage();

        testee.tileIdFilter = TILE_A.tileId;
        leafletMap().setZoom(14, {animate: false});

        expectTileStyle(TILE_A, false);
    });
});
