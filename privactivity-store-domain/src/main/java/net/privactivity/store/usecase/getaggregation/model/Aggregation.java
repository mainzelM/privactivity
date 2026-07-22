package net.privactivity.store.usecase.getaggregation.model;

import java.time.Duration;

public sealed interface Aggregation permits MonthlyAggregation, YearlyAggregation, TotalAggregation {
    int km();

    Duration duration();
}
