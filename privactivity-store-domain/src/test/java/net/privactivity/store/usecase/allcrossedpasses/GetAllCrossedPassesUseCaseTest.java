package net.privactivity.store.usecase.allcrossedpasses;

import net.privactivity.domain.Activity;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.model.ListBasedMountainPassRepository;
import net.privactivity.store.model.MapBasedActivityRepository;
import net.privactivity.store.usecase.crossedpasses.MountainPass;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;



import static org.assertj.core.api.Assertions.assertThat;

class GetAllCrossedPassesUseCaseTest {

    private static final MountainPass PASS_A = new MountainPass("Pass A", "AT", new LatLon(47.1000, 11.1000));
    private static final MountainPass PASS_B = new MountainPass("Pass B", "AT", new LatLon(47.2000, 11.2000));

    @Test
    void act_returnsNoCrossingsWhenNoActivityIsCloseToAnyPass() {
        Activity activity = activity(1, ZonedDateTime.parse("2024-01-01T10:00:00Z"), List.of(
                waypoint(0, 50.0000, 10.0000)));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(1L, activity));
        ListBasedMountainPassRepository passRepository = new ListBasedMountainPassRepository(List.of(PASS_A));
        GetAllCrossedPassesUseCase testee = new GetAllCrossedPassesUseCase(activityRepository, passRepository);

        AllCrossedPasses result = testee.act();

        assertThat(result.allPassCrossings()).isEmpty();
    }

    @Test
    void act_createsSingleCrossingWhenOneActivityCrossesOnePass() {
        Activity activity = activity(1, ZonedDateTime.parse("2024-01-01T10:00:00Z"), List.of(
                waypoint(0, 47.1000, 11.1000)));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(1L, activity));
        ListBasedMountainPassRepository passRepository = new ListBasedMountainPassRepository(List.of(PASS_A));
        GetAllCrossedPassesUseCase testee = new GetAllCrossedPassesUseCase(activityRepository, passRepository);

        AllCrossedPasses result = testee.act();

        assertThat(result.allPassCrossings()).containsExactly(new PassCrossing(PASS_A, activity, activity));
    }

    @Test
    void act_tracksFirstAndLastCrossingAcrossMultipleActivities() {
        Activity earliest = activity(1, ZonedDateTime.parse("2024-01-01T10:00:00Z"), List.of(
                waypoint(0, 47.1000, 11.1000)));
        Activity middle = activity(2, ZonedDateTime.parse("2024-01-05T10:00:00Z"), List.of(
                waypoint(0, 47.1000, 11.1000)));
        Activity latest = activity(3, ZonedDateTime.parse("2024-01-10T10:00:00Z"), List.of(
                waypoint(0, 47.1000, 11.1000)));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(
                Map.of(1L, earliest, 2L, middle, 3L, latest));
        ListBasedMountainPassRepository passRepository = new ListBasedMountainPassRepository(List.of(PASS_A));
        GetAllCrossedPassesUseCase testee = new GetAllCrossedPassesUseCase(activityRepository, passRepository);

        AllCrossedPasses result = testee.act();

        assertThat(result.allPassCrossings()).containsExactly(new PassCrossing(PASS_A, earliest, latest));
    }

    @Test
    void act_createsSeparateCrossingsForEachCrossedPass() {
        Activity activity = activity(1, ZonedDateTime.parse("2024-01-01T10:00:00Z"), List.of(
                waypoint(0, 47.1000, 11.1000),
                waypoint(60, 47.2000, 11.2000)));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(1L, activity));
        ListBasedMountainPassRepository passRepository = new ListBasedMountainPassRepository(List.of(PASS_A, PASS_B));
        GetAllCrossedPassesUseCase testee = new GetAllCrossedPassesUseCase(activityRepository, passRepository);

        AllCrossedPasses result = testee.act();

        assertThat(result.allPassCrossings()).containsExactlyInAnyOrder(
                new PassCrossing(PASS_A, activity, activity),
                new PassCrossing(PASS_B, activity, activity));
    }

    @Test
    void act_skipsWaypointsWithoutLatLonWithoutThrowing() {
        Activity activity = activity(1, ZonedDateTime.parse("2024-01-01T10:00:00Z"), List.of(
                waypointWithoutLocation(0),
                waypoint(60, 47.1000, 11.1000)));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(1L, activity));
        ListBasedMountainPassRepository passRepository = new ListBasedMountainPassRepository(List.of(PASS_A));
        GetAllCrossedPassesUseCase testee = new GetAllCrossedPassesUseCase(activityRepository, passRepository);

        AllCrossedPasses result = testee.act();

        assertThat(result.allPassCrossings()).containsExactly(new PassCrossing(PASS_A, activity, activity));
    }

    private Activity activity(long id, ZonedDateTime start, List<Waypoint> waypoints) {
        return Activity.builder()
                       .id(id)
                       .title("test")
                       .start(start)
                       .waypoints(waypoints)
                       .totals(new Activity.Totals(Duration.ZERO))
                       .build();
    }

    private Waypoint waypoint(int secondsSinceStart, double lat, double lon) {
        return new Waypoint(secondsSinceStart,
                            Maybe.some(new LatLon(lat, lon)),
                            Maybe.none(),
                            Maybe.none(),
                            Maybe.none(),
                            Maybe.none(),
                            Maybe.none(),
                            Maybe.none(),
                            1);
    }

    private Waypoint waypointWithoutLocation(int secondsSinceStart) {
        return new Waypoint(secondsSinceStart,
                            Maybe.none(),
                            Maybe.none(),
                            Maybe.none(),
                            Maybe.none(),
                            Maybe.none(),
                            Maybe.none(),
                            Maybe.none(),
                            1);
    }
}
