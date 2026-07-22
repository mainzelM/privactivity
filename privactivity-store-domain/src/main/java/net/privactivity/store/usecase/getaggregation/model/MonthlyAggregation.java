package net.privactivity.store.usecase.getaggregation.model;

import java.time.Duration;

public record MonthlyAggregation(int km, Duration duration) implements Aggregation {
}
