export interface MaxPower {
    maxPowers: {
        [duration: string]: MaxPowerEvent;
    };
}

export interface LatLon {
    lat: number;
    lon: number;
}

export interface Waypoint {
    secondsSinceStart: number;
    latlon?: LatLon | null;
    speedInMeterPerHour?: number | null;
    power?: number | null;
    heartRate?: number | null;
    cadence?: number | null;
    altitude: Maybe;
    distanceInMeter: Maybe;
}

export interface Maybe {
    type: string;
    value?: number | null;
}

export interface MaxPowerEvent {
    watts: number;
    wp: Waypoint;
    activityId: number;
    timestamp: string; // ISO 8601 date string
}

