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

        Map<LocalDate, ComputeMovingSumUseCase.MovingSum> result = testee.compute(7);

        assertThat(result).isEmpty();
    }

    @Test
    void singleActivityWithDistance() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1, "Run 1", dateTime(2023, 7, 1), 5000)));
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2023, 7, 8));

        Map<LocalDate, ComputeMovingSumUseCase.MovingSum> result = testee.compute(7);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                LocalDate.of(2023, 7, 7), movingSum(5000, 1, 0),
                LocalDate.of(2023, 7, 8), movingSum(0, 0, 0)));
    }

    @Test
    void multipleActivitiesInSameWindow() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1, "Run 1", dateTime(2024, 8, 1), 5000),
                2L, activity(2, "Run 2", dateTime(2024, 8, 2), 6000),
                3L, activity(3, "Run 3", dateTime(2024, 8, 3), 4000)));
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2024, 8, 4));

        Map<LocalDate, ComputeMovingSumUseCase.MovingSum> result = testee.compute(3);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                LocalDate.of(2024, 8, 3), movingSum(15000, 3, 0),
                LocalDate.of(2024, 8, 4), movingSum(10000, 2, 0)));
    }

    @Test
    void activitiesAcrossMultipleWindows() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1, "Run 1", dateTime(2024, 8, 1), 5000),
                2L, activity(2, "Run 2", dateTime(2024, 8, 2), 6000),
                3L, activity(3, "Run 3", dateTime(2024, 8, 3), 4000),
                4L, activity(4, "Run 4", dateTime(2024, 8, 4), 7000)));
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2024, 8, 4));

        Map<LocalDate, ComputeMovingSumUseCase.MovingSum> result = testee.compute(2);

        assertThat(result)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        LocalDate.of(2024, 8, 2), movingSum(11000, 2, 0),
                        LocalDate.of(2024, 8, 3), movingSum(10000, 2, 0),
                        LocalDate.of(2024, 8, 4), movingSum(11000, 2, 0)));
    }

    @Test
    void activitiesWithoutDistanceAreIgnored() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1, "Run 1", dateTime(2024, 8, 1), 5000),
                2L, activityNoDistance(2, dateTime(2024, 8, 2)),
                3L, activity(3, "Run 3", dateTime(2024, 8, 3), 4000)));
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2024, 8, 4));

        Map<LocalDate, ComputeMovingSumUseCase.MovingSum> result = testee.compute(3);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                LocalDate.of(2024, 8, 3), movingSum(9000, 3, 0),
                LocalDate.of(2024, 8, 4), movingSum(4000, 2, 0)));
    }

    @Test
    void windowSmallerThanDataSpan() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1, "Run 1", dateTime(2024, 8, 1), 5000),
                2L, activity(2, "Run 2", dateTime(2024, 8, 2), 6000),
                3L, activity(3, "Run 3", dateTime(2024, 8, 3), 4000)));
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2024, 8, 4));

        Map<LocalDate, ComputeMovingSumUseCase.MovingSum> result = testee.compute(1);

        assertThat(result)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        LocalDate.of(2024, 8, 1), movingSum(5000, 1, 0),
                        LocalDate.of(2024, 8, 2), movingSum(6000, 1, 0),
                        LocalDate.of(2024, 8, 3), movingSum(4000, 1, 0),
                        LocalDate.of(2024, 8, 4), movingSum(0, 0, 0)));
    }

    @Test
    void sumsDistanceMovingTimeAndAscent() {
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(
                1L, activity(1, "Run 1", dateTime(2024, 8, 1), 5000, 1000, Duration.ofMinutes(60)),
                2L, activity(2, "Run 2", dateTime(2024, 8, 2), 6000, 2000, Duration.ofMinutes(120))));
        testee = new ComputeMovingSumUseCase(activityRepository, LocalDate.of(2024, 8, 2));

        Map<LocalDate, ComputeMovingSumUseCase.MovingSum> result = testee.compute(2);

        assertThat(result.get(LocalDate.of(2024, 8, 2)))
                .isEqualTo(movingSum(11000, 3, 3));
    }

    private Activity activity(long id, String title, ZonedDateTime start, int distanceInMeters) {
        return activity(id, title, start, distanceInMeters, 0, Duration.ofHours(1));
    }

    private Activity activity(long id, String title, ZonedDateTime start, int distanceInMeters, int ascent,
                              Duration movingTime) {
        return Activity.builder()
                       .id(id)
                       .title(title)
                       .start(start)
                       .waypoints(List.of())
                       .totals(new Activity.Totals(
                               some(distanceInMeters),
                               some(ascent),
                               none(),
                               some(movingTime),
                               none()
                       ))
                       .bounds(Bounds.of(0d, 0d, 0d, 0d))
                       .build();
    }

    private ComputeMovingSumUseCase.MovingSum movingSum(int distanceInMeters, int movingTimeInHours,
                                                        int ascentInKilometers) {
        return new ComputeMovingSumUseCase.MovingSum(distanceInMeters, movingTimeInHours, ascentInKilometers);
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
