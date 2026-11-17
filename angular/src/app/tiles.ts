export type TileMetric = 'activityCount' | 'maxPower' | 'maxHeartRate' | 'averagePower' | 'averageHeartRate';

export const TILE_METRICS: ReadonlyArray<{ value: TileMetric; label: string }> = [
    {value: 'activityCount', label: 'Activities'},
    {value: 'maxPower', label: 'Max Power'},
    {value: 'maxHeartRate', label: 'Max Heart Rate'},
    {value: 'averagePower', label: 'Average Power'},
    {value: 'averageHeartRate', label: 'Average Heart Rate'}
];

export interface TileBounds {
    south: number;
    north: number;
    west: number;
    east: number;
}

export interface TileSummary {
    tileId: string;
    bounds: TileBounds;
    firstActivityId: number | null;
    latestActivityId: number | null;
    maxPower: number | null;
    maxHeartRate: number | null;
    averagePower: number | null;
    averageHeartRate: number | null;
    activityIds: number[];
}

export interface TileLegend {
    // The Java enum constant, e.g. 'ACTIVITY_COUNT'. It is not a TileMetric, so remember the requested metric instead.
    metric: string;
    min: number | null;
    max: number | null;
    quantiles: number[];
}

export interface TilesResponse {
    // See TileLegend.metric.
    metric: string;
    legend: TileLegend;
    tileWidthDegrees: number;
    tileHeightDegrees: number;
    generatedAt: string | null;
    tiles: TileSummary[];
}