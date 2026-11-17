import {PathOptions, Rectangle, rectangle} from 'leaflet';
import {TILE_METRICS, TileLegend, TileMetric, TileSummary} from '../tiles';

const TILE_TEMPERATURE_COLORS = [
    '#2b3f8f', '#2553a0', '#1f67b0', '#197bc0', '#138fd1',
    '#0da3e1', '#16b8dc', '#28c8c4', '#3ad6ab', '#5be08f',
    '#84e56f', '#afe85b', '#d7e955', '#efe04f', '#f7c949',
    '#f8ae43', '#f88f3e', '#f66f39', '#f14f35', '#e63b2f'
];

const TILE_NEUTRAL_COLOR = '#9fb3bf';

const MIN_BORDER_ZOOM = 6;
const MAX_BORDER_ZOOM = 16;

export function tileMetricLabel(metric: TileMetric): string {
    return TILE_METRICS.find(option => option.value === metric)?.label ?? metric;
}

export function tileMetricValue(tile: TileSummary, metric: TileMetric): number | null {
    switch (metric) {
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

export function tileBorderColor(value: number | null, legend: TileLegend): string {
    if (value === null || legend.quantiles.length === 0) {
        return TILE_NEUTRAL_COLOR;
    }

    const thresholdIndex = legend.quantiles.findIndex(quantile => value <= quantile);
    if (thresholdIndex === -1) {
        return TILE_TEMPERATURE_COLORS[TILE_TEMPERATURE_COLORS.length - 1];
    }
    return TILE_TEMPERATURE_COLORS[thresholdIndex];
}

export function tileBorderWeight(zoom: number, selected: boolean): number {
    const baseWeight = 2 + (normalizeZoom(zoom) * 3);
    return selected ? baseWeight + 2 : baseWeight;
}

export function tileTooltip(tile: TileSummary, metric: TileMetric): string {
    const value = tileMetricValue(tile, metric);
    return `${tile.activityIds.length} Activities${value !== null ? ` • ${tileMetricLabel(metric)}: ${value.toFixed(1)}` : ''}`;
}

/**
 * Border-only style: the fill is fully transparent but still painted, so that the whole tile reacts to the mouse.
 */
export function tileOverlayStyle(tile: TileSummary, legend: TileLegend, metric: TileMetric, zoom: number,
                                 selected: boolean): PathOptions {
    return {
        color: tileBorderColor(tileMetricValue(tile, metric), legend),
        opacity: selected ? 0.9 : 0.6,
        weight: tileBorderWeight(zoom, selected),
        fill: true,
        fillOpacity: 0,
        fillColor: 'transparent'
    };
}

export function createTileRectangle(tile: TileSummary, metric: TileMetric, options: PathOptions): Rectangle {
    const layer = rectangle([
        [tile.bounds.south, tile.bounds.west],
        [tile.bounds.north, tile.bounds.east]
    ], options);
    layer.bindTooltip(tileTooltip(tile, metric), {sticky: true});
    return layer;
}

function normalizeZoom(zoom: number): number {
    if (zoom <= MIN_BORDER_ZOOM) {
        return 0;
    }
    if (zoom >= MAX_BORDER_ZOOM) {
        return 1;
    }
    return (zoom - MIN_BORDER_ZOOM) / (MAX_BORDER_ZOOM - MIN_BORDER_ZOOM);
}
