package net.privactivity.store.aggregator;

import net.privactivity.store.usecase.getaggregation.model.Aggregation;

public record AggregationDTO(int km, int minutes) {
    public AggregationDTO(Aggregation aggregation) {
        this(aggregation.km(), (int) aggregation.duration().toMinutes());
    }
}
