package net.privactivity.store.usecase.getaggregation.model;

import java.time.Duration;
import java.util.NavigableMap;

public record YearlyAggregation(
        NavigableMap<Month, MonthlyAggregation> byMonth) implements Aggregation {
    public int km() {
        return byMonth.values().stream().map(Aggregation::km).reduce(0, Integer::sum);
    }

    public Duration duration() {
        return byMonth.values().stream().map(Aggregation::duration).reduce(Duration.ZERO,
                                                                           Duration::plus);
    }
}
