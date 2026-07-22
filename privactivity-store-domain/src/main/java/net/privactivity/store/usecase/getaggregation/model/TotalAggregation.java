package net.privactivity.store.usecase.getaggregation.model;

import java.time.Duration;
import java.util.NavigableMap;

public record TotalAggregation(
        NavigableMap<Year, YearlyAggregation> byYear) implements Aggregation {
    public int km() {
        return byYear.values().stream().map(Aggregation::km).reduce(0, Integer::sum);
    }

    public Duration duration() {
        return byYear.values().stream().map(Aggregation::duration).reduce(Duration.ZERO,
                                                                          Duration::plus);
    }
}
