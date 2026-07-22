package net.privactivity.store.aggregator;

import net.privactivity.store.usecase.getaggregation.model.Aggregation;

public record AggregationDTO(int km, int minutes, int eddingtonNumber) {
    public AggregationDTO(Aggregation aggregation, int eddingtonNumber) {
        this(aggregation.km(), (int) aggregation.duration().toMinutes(), eddingtonNumber);
    }
}
