package net.privactivity.store.usecase.getaggregation;

import net.privactivity.domain.Activity;
import net.privactivity.store.usecase.getaggregation.model.ActivityAggregatorDomainService;
import net.privactivity.store.usecase.getaggregation.model.Month;
import net.privactivity.store.usecase.getaggregation.model.MonthlyAggregation;
import net.privactivity.store.usecase.getaggregation.model.TotalAggregation;
import net.privactivity.store.usecase.getaggregation.model.Year;
import net.privactivity.store.usecase.getaggregation.model.YearlyAggregation;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.List;
import java.util.NavigableMap;
import java.util.function.Predicate;



import static org.assertj.core.api.Assertions.assertThat;

class ActivityAggregatorDomainServiceTest extends ActivityTest {

    private final ActivityAggregatorDomainService testee = new ActivityAggregatorDomainService();


    @Test
    void test_empty_aggregation() {
        TotalAggregation aggregate = testee.aggregate(List.of());

        assertThat(aggregate.byYear()).isEmpty();
    }

    @Test
    void test_aggregates_two_activities_same_month() {
        List<Activity> activitites = List.of(a6, a7);
        TotalAggregation aggregate = testee.aggregate(activitites);

        NavigableMap<Year, YearlyAggregation> byYear = aggregate.byYear();
        assertThat(byYear).containsOnlyKeys(y2027);
        assertThat(byYear.get(y2027).km()).isEqualTo(66 + 77);
        assertThat(byYear.get(y2027).duration()).isEqualTo(Duration.ofMinutes(660 + 770));

        NavigableMap<Month, MonthlyAggregation> y2027ByMonth =
                byYear.get(y2027).byMonth();
        Month june = new Month(6);
        assertThat(y2027ByMonth).containsOnlyKeys(june);
        assertThat(y2027ByMonth.get(june).km()).isEqualTo(66 + 77);
        assertThat(y2027ByMonth.get(june).duration()).isEqualTo(Duration.ofMinutes(660 + 770));
    }

    @Test
    void test_aggregates_two_activities_same_year() {
        List<Activity> activitites = List.of(a1, a2);
        TotalAggregation aggregate = testee.aggregate(activitites);

        NavigableMap<Year, YearlyAggregation> byYear = aggregate.byYear();
        assertThat(byYear).containsOnlyKeys(y2024);
        assertThat(byYear.get(y2024).km()).isEqualTo(15 + 27);
        assertThat(byYear.get(y2024).duration()).isEqualTo(Duration.ofMinutes(150 + 270));
    }

    @Test
    void test_aggregates_multiple_years() {
        List<Activity> activitites = List.of(a1, a2, a3, a4, a5, a6);
        TotalAggregation aggregate = testee.aggregate(activitites);

        NavigableMap<Year, YearlyAggregation> byYear = aggregate.byYear();
        assertThat(byYear).containsOnlyKeys(y2024, y2026, y2027);
        assertThat(byYear.get(y2024).km()).isEqualTo(15 + 27);
        assertThat(byYear.get(y2026).km()).isEqualTo(30 + 41);
        assertThat(byYear.get(y2027).km()).isEqualTo(55 + 66);
        assertThat(byYear.get(y2024).duration()).isEqualTo(Duration.ofMinutes(150 + 270));
        assertThat(byYear.get(y2026).duration()).isEqualTo(Duration.ofMinutes(300 + 410));
        assertThat(byYear.get(y2027).duration()).isEqualTo(Duration.ofMinutes(550 + 660));
    }

    @Test
    void test_aggregates_filtered() {
        List<Activity> activitites = List.of(a1, a2, a3, a4);
        Predicate<Activity> filter = a -> a.start().getMonth().getValue() < 3;
        TotalAggregation aggregate =
                testee.aggregateFiltered(activitites, filter);

        NavigableMap<Year, YearlyAggregation> byYear = aggregate.byYear();
        assertThat(byYear).containsOnlyKeys(y2024, y2026);
        assertThat(byYear.get(y2024).km()).isEqualTo(15 + 27);
        assertThat(byYear.get(y2026).km()).isEqualTo(30);
        assertThat(byYear.get(y2024).duration()).isEqualTo(Duration.ofMinutes(150 + 270));
        assertThat(byYear.get(y2026).duration()).isEqualTo(Duration.ofMinutes(300));
    }
}