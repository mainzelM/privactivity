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