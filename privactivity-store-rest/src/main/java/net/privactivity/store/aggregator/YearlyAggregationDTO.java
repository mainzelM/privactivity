package net.privactivity.store.aggregator;

import net.privactivity.store.usecase.getaggregation.model.Month;
import net.privactivity.store.usecase.getaggregation.model.MonthlyAggregation;
import net.privactivity.store.usecase.getaggregation.model.YearlyAggregation;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

public record YearlyAggregationDTO(Map<Integer, AggregationDTO> months, AggregationDTO totals) {

    YearlyAggregationDTO(YearlyAggregation yearlyAggregation) {
        this(toMap(yearlyAggregation.byMonth()), new AggregationDTO(yearlyAggregation, 0));
    }

    private static Map<Integer, AggregationDTO> toMap(NavigableMap<Month,
            MonthlyAggregation> monthMonthlyAggregationNavigableMap) {
        Map<Integer, AggregationDTO> result = new HashMap<>();
        monthMonthlyAggregationNavigableMap.forEach((month, monthlyAggregation) ->
                                                            result.put(month.month(),
                                                                       new AggregationDTO(monthlyAggregation, 0)));
        return result;
    }
}
