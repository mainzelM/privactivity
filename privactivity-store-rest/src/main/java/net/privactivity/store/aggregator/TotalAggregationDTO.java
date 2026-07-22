package net.privactivity.store.aggregator;

import net.privactivity.store.usecase.getaggregation.model.TotalAggregation;
import java.util.HashMap;
import java.util.Map;

record TotalAggregationDTO(Map<Integer, YearlyAggregationDTO> years, AggregationDTO totals) {


    TotalAggregationDTO(TotalAggregation totalAggregation, int eddingtonNumber) {
        this(toMap(totalAggregation), new AggregationDTO(totalAggregation, eddingtonNumber));
    }

    private static Map<Integer, YearlyAggregationDTO> toMap(TotalAggregation totalAggregation) {
        Map<Integer, YearlyAggregationDTO> result = new HashMap<>();
        totalAggregation.byYear().forEach((key, value) ->
                                                  result.put(key.year(), new YearlyAggregationDTO(value)));
        return result;
    }
}
