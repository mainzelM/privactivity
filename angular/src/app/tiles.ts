export type TileMetric = 'activityCount' | 'maxPower' | 'maxHeartRate' | 'averagePower' | 'averageHeartRate';

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
    metric: TileMetric;
    min: number | null;
    max: number | null;
    quantiles: number[];
}

export interface TilesResponse {
    metric: TileMetric;
    legend: TileLegend;
    tileWidthDegrees: number;
    tileHeightDegrees: number;
    generatedAt: string | null;
    tiles: TileSummary[];
}