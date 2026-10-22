export interface Aggregation {
    km: number;
    minutes: number;
    eddingtonNumber: number;
}

export interface YearlyAggregation {
    months: { [key: string]: Aggregation };
    totals: Aggregation;
}

export interface TotalAggregation {
    years: { [key: string]: YearlyAggregation };
    totals: Aggregation;
}

export interface MovingSumValues {
    distanceInMeters: number;
    movingTimeInHours: number;
    ascentInKilometers: number;
}

export type MovingSum = { [date: string]: MovingSumValues };

export interface EddigtionChart {
    countsPerKM: number[];
    eddigtionNumber: number;
    missingToNext: number;
}