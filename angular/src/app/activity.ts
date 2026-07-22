export interface Activity {
    id: number;
    title: string;
    start: string; // ISO 8601 date string
    waypoints: Waypoint[];
    totals: Totals;
    averages: Averages;
    maxima: Maxima;
    bounds: Bounds;
    summary: Summary;
}

export interface Summary {
    trainingStressScore: number | null;
    totalTrainingEffect: number | null;
    intensityFactor: number | null;
    avgLeftPco: number | null;
    avgRightPco: number | null;
}

export interface Totals {
    distance: number;
    duration: number;
    ascent: number;
    descent: number;
}

export interface Averages {
    speedInMetersPerHour: number;
    cadence: number;
    heartRate: number;
    power: number;
}

export interface Maxima {
    speedInMetersPerHour: number;
    heartRate: number;
    power: number;
    altitude: number;
}

export interface Bounds {
    south: number;
    north: number;
    west: number;
    east: number;
}

export interface Waypoint {
    secondsSinceStart: number;
    latlon: LatLon | null;
    speedInMeterPerHour: number;
    power: number;
    heartRate: number;
    cadence: number;
    altitude: number;
    distanceInMeter: number;
}

export interface LatLon {
    lat: number;
    lon: number;
}