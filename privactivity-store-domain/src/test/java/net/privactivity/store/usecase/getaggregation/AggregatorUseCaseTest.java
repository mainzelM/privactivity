package net.privactivity.store.usecase.getaggregation;

import net.privactivity.domain.Activity;
import net.privactivity.store.model.MapBasedActivityRepository;
import net.privactivity.store.usecase.getaggregation.model.Month;
import net.privactivity.store.usecase.getaggregation.model.TotalAggregation;
import net.privactivity.store.usecase.getaggregation.model.Year;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

import static net.privactivity.domain.Maybe.none;
import static net.privactivity.domain.Maybe.some;
import static org.assertj.core.api.Assertions.assertThat;
class AggregatorUseCaseTest {

    @Test
    void aggregateYTD_includes_activity_from_today() {
        ZonedDateTime latest = ZonedDateTime.now();
        Activity today = Activity.builder()
                                 .id(1L)
                                 .title("Today")
                                 .start(latest)
                                 .totals(new Activity.Totals(some(1000), none(), none(), some(Duration.ofMinutes(30)),
                                                             none()))
                                 .build();

        AggregatorUseCase testee = new AggregatorUseCase(new MapBasedActivityRepository(Map.of(1L, today)));

        TotalAggregation aggregate = testee.aggregateYTD();

        Year year = new Year(latest.getYear());
        Month month = new Month(latest.getMonthValue());

        assertThat(aggregate.byYear()).containsOnlyKeys(year);
        assertThat(aggregate.byYear().get(year).byMonth()).containsOnlyKeys(month);
        assertThat(aggregate.byYear().get(year).byMonth().get(month).km()).isEqualTo(1);
        assertThat(aggregate.byYear().get(year).byMonth().get(month).duration()).isEqualTo(Duration.ofMinutes(30));
    }
}
