export interface ActivityFilterRequest {
    includeWaypoints?: boolean;
    minDistance?: number;
    maxDistance?: number;
    minAscent?: number;
    maxAscent?: number;
    tileId?: string;
    lat?: number;
    lon?: number;
    radius?: number;
    title?: string;
    sortBy?: string;
    sortDirection?: string;
}
