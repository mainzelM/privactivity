package net.privactivity.store.usecase.getaggregation;

import net.privactivity.domain.Activity;
import net.privactivity.store.usecase.getaggregation.model.ActivityAggregatorDomainService;
import net.privactivity.store.usecase.getaggregation.model.AggregationFilter;
import net.privactivity.store.usecase.getaggregation.model.Month;
import net.privactivity.store.usecase.getaggregation.model.MonthlyAggregation;
import net.privactivity.store.usecase.getaggregation.model.TotalAggregation;
import org.junit.jupiter.api.Test;
import java.util.Arrays;



import static org.assertj.core.api.Assertions.assertThat;

class AggregationFilterTest extends ActivityTest {

    @Test
    void empty_filter_passes_through() {
        AggregationFilter filter = new AggregationFilter();

        TotalAggregation totals = makeTotalAggregation(a1, a2);
        TotalAggregation filtered = filter.filterByMonths(totals);

        assertThat(filtered).isEqualTo(totals);
    }

    @Test
    void filter_all_months_passes_through() {
        AggregationFilter filter = new AggregationFilter();

        TotalAggregation totals = makeTotalAggregation(a1, a2, a3, a4, a5, a6, a7);
        TotalAggregation filtered = filter.filterByMonths(totals, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                                                          11, 12);

        assertThat(filtered).isEqualTo(totals);
    }

    @Test
    void unused_months_filters_out_everything() {
        AggregationFilter filter = new AggregationFilter();

        TotalAggregation totals = makeTotalAggregation(a1, a2);
        TotalAggregation filtered = filter.filterByMonths(totals, 12);

        assertThat(filtered.byYear()).containsOnlyKeys(y2024);
        assertThat(filtered.byYear().get(y2024).byMonth()).isEmpty();
    }

    @Test
    void filter_one_month() {
        AggregationFilter filter = new AggregationFilter();

        TotalAggregation totals = makeTotalAggregation(a1, a2, a3, a4, a5, a6, a7);
        TotalAggregation filtered = filter.filterByMonths(totals, 6);

        assertThat(filtered.byYear()).containsOnlyKeys(y2024, y2026, y2027);
        assertThat(filtered.byYear().get(y2024).byMonth()).isEmpty();
        assertThat(filtered.byYear().get(y2026).byMonth()).isEmpty();
        assertThat(filtered.byYear().get(y2027).byMonth()).containsOnlyKeys(new Month(6));
        MonthlyAggregation june2027 =
                filtered.byYear().get(y2027).byMonth().get(new Month(6));
        assertThat(june2027.km()).isEqualTo(66 + 77);
    }

    private TotalAggregation makeTotalAggregation(Activity... activities) {
        ActivityAggregatorDomainService aggregator = new ActivityAggregatorDomainService();
        return aggregator.aggregate(Arrays.asList(activities));
    }
}