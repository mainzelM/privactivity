package net.privactivity.store.usecase.getaggregation.model;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class AggregationFilter {

    public TotalAggregation filterByMonths(TotalAggregation aggregation,
                                           int... months) {
        if (months.length == 0) {
            return aggregation;
        } else {
            List<Month> monthList = Arrays.stream(months)
                                          .mapToObj(Month::new)
                                          .toList();
            NavigableMap<Year, YearlyAggregation> filtered = new TreeMap<>();
            aggregation.byYear().forEach((year, agg) -> {
                NavigableMap<Month, MonthlyAggregation> byMonthFiltered = new TreeMap<>();
                agg.byMonth().sequencedKeySet().stream()
                   .filter(monthList::contains)
                   .forEach(month -> byMonthFiltered.put(month, agg.byMonth().get(month)));
                filtered.put(year, new YearlyAggregation(byMonthFiltered));
            });
            return new TotalAggregation(filtered);
        }
    }
}
