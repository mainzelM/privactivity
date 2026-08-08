package net.privactivity.store.usecase.eddingtion;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Bounds;
import net.privactivity.store.model.MapBasedActivityRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;



import static net.privactivity.domain.Maybe.none;
import static net.privactivity.domain.Maybe.some;
import static org.assertj.core.api.Assertions.assertThat;

class ComputeEddingtonChartUseCaseTest {

    @Test
    void computesEddingtonChartFromMapBasedActivityRepository() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1L, 1000, 1),
                2L, activity(2L, 2000, 2),
                3L, activity(3L, 4000, 3),
                4L, activityWithoutDistance(4L, 4)
                                                                                             ));
        ComputeEddingtonChartUseCase testee = new ComputeEddingtonChartUseCase(activityRepository);

        ComputeEddingtonChartUseCase.EddigtionChart result = testee.act();

        assertThat(result.countsPerKM()).containsExactly(4, 3, 2, 1, 1);
    }

    private Activity activity(long id, int distanceInMeters, int dayOfMonth) {
        return Activity.builder()
                       .id(id)
                       .title("Activity " + id)
                       .start(ZonedDateTime.of(2024, 1, dayOfMonth, 12, 0, 0, 0, ZoneId.of("UTC")))
                       .waypoints(List.of())
                       .totals(new Activity.Totals(
                               some(distanceInMeters),
                               none(),
                               none(),
                               some(Duration.ofMinutes(30)),
                               none()
                       ))
                       .bounds(Bounds.of(0d, 0d, 0d, 0d))
                       .build();
    }

    private Activity activityWithoutDistance(long id, int dayOfMonth) {
        return Activity.builder()
                       .id(id)
                       .title("Activity " + id)
                       .start(ZonedDateTime.of(2024, 1, dayOfMonth, 12, 0, 0, 0, ZoneId.of("UTC")))
                       .waypoints(List.of())
                       .totals(new Activity.Totals(
                               none(),
                               none(),
                               none(),
                               some(Duration.ofMinutes(30)),
                               none()
                       ))
                       .bounds(Bounds.of(0d, 0d, 0d, 0d))
                       .build();
    }
}
