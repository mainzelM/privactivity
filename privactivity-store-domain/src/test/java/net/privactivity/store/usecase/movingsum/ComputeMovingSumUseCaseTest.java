package net.privactivity.store.usecase.movingsum;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Bounds;
import net.privactivity.store.model.MapBasedActivityRepository;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;



import static net.privactivity.domain.Maybe.none;
import static net.privactivity.domain.Maybe.some;
import static org.assertj.core.api.Assertions.assertThat;

class ComputeMovingSumUseCaseTest {

    private ComputeMovingSumUseCase testee;

    @Test
    void emptyRepositoryReturnsEmptyMap() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2023, 7, 8));

        Map<LocalDate, Integer> result = testee.compute(7);

        assertThat(result).isEmpty();
    }

    @Test
    void singleActivityWithDistance() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1, "Run 1", dateTime(2023, 7, 1), 5000)));
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2023, 7, 8));

        Map<LocalDate, Integer> result = testee.compute(7);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                LocalDate.of(2023, 7, 7), 5000,
                LocalDate.of(2023, 7, 8), 0));
    }

    @Test
    void multipleActivitiesInSameWindow() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1, "Run 1", dateTime(2024, 8, 1), 5000),
                2L, activity(2, "Run 2", dateTime(2024, 8, 2), 6000),
                3L, activity(3, "Run 3", dateTime(2024, 8, 3), 4000)));
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2024, 8, 4));

        Map<LocalDate, Integer> result = testee.compute(3);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                LocalDate.of(2024, 8, 3), 15000,
                LocalDate.of(2024, 8, 4), 10000));
    }

    @Test
    void activitiesAcrossMultipleWindows() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1, "Run 1", dateTime(2024, 8, 1), 5000),
                2L, activity(2, "Run 2", dateTime(2024, 8, 2), 6000),
                3L, activity(3, "Run 3", dateTime(2024, 8, 3), 4000),
                4L, activity(4, "Run 4", dateTime(2024, 8, 4), 7000)));
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2024, 8, 4));

        Map<LocalDate, Integer> result = testee.compute(2);

        assertThat(result)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        LocalDate.of(2024, 8, 2), 11000,
                        LocalDate.of(2024, 8, 3), 10000,
                        LocalDate.of(2024, 8, 4), 11000));
    }

    @Test
    void activitiesWithoutDistanceAreIgnored() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1, "Run 1", dateTime(2024, 8, 1), 5000),
                2L, activityNoDistance(2, dateTime(2024, 8, 2)),
                3L, activity(3, "Run 3", dateTime(2024, 8, 3), 4000)));
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2024, 8, 4));

        Map<LocalDate, Integer> result = testee.compute(3);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                LocalDate.of(2024, 8, 3), 9000,
                LocalDate.of(2024, 8, 4), 4000));
    }

    @Test
    void windowSmallerThanDataSpan() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1, "Run 1", dateTime(2024, 8, 1), 5000),
                2L, activity(2, "Run 2", dateTime(2024, 8, 2), 6000),
                3L, activity(3, "Run 3", dateTime(2024, 8, 3), 4000)));
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2024, 8, 4));

        Map<LocalDate, Integer> result = testee.compute(1);

        assertThat(result)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        LocalDate.of(2024, 8, 1), 5000,
                        LocalDate.of(2024, 8, 2), 6000,
                        LocalDate.of(2024, 8, 3), 4000,
                        LocalDate.of(2024, 8, 4), 0));
    }

    private Activity activity(long id, String title, ZonedDateTime start, int distanceInMeters) {
        return Activity.builder()
                       .id(id)
                       .title(title)
                       .start(start)
                       .waypoints(List.of())
                       .totals(new Activity.Totals(
                               some(distanceInMeters),
                               none(),
                               none(),
                               some(Duration.ofHours(1)),
                               none()
                       ))
                       .bounds(Bounds.of(0d, 0d, 0d, 0d))
                       .build();
    }

    @SuppressWarnings("SameParameterValue")
    private Activity activityNoDistance(long id, ZonedDateTime start) {
        return Activity.builder()
                       .id(id)
                       .title("Run 2")
                       .start(start)
                       .waypoints(List.of())
                       .totals(new Activity.Totals(
                               none(),
                               none(),
                               none(),
                               some(Duration.ofHours(1)),
                               none()
                       ))
                       .bounds(Bounds.of(0d, 0d, 0d, 0d))
                       .build();
    }

    private ZonedDateTime dateTime(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 12, 0, 0, 0, ZoneId.of("UTC"));
    }
}
