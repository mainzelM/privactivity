package net.privactivity.store.usecase.crossedpasses;

import net.privactivity.domain.Activity;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.model.ListBasedMountainPassRepository;
import net.privactivity.store.model.MapBasedActivityRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;



import static org.assertj.core.api.Assertions.assertThat;

class GetPassesTestCrossing {

    private static final long ACTIVITY_ID = 1L;

    @Test
    void act_returnsPassedPassesForWaypointsCloseToKnownPasses() {
        Activity activity = Activity.builder()
                                    .id(ACTIVITY_ID)
                                    .title("test")
                                    .start(ZonedDateTime.now())
                                    .waypoints(List.of(
                                            waypoint(0, 47.1000, 11.1000),
                                            waypointWithoutLocation(60),
                                            waypoint(120, 47.2000, 11.2000)))
                                    .totals(new Activity.Totals(Duration.ZERO))
                                    .build();
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(ACTIVITY_ID, activity));
        ListBasedMountainPassRepository passRepository = new ListBasedMountainPassRepository(List.of(
                new MountainPass("Pass A", "AT", new LatLon(47.1000, 11.1000)),
                new MountainPass("Far Away", "AT", new LatLon(50.0000, 10.0000)),
                new MountainPass("Pass B", "AT", new LatLon(47.2000, 11.2000))
                                                                                                    ));
        GetCrossedPassesUseCase testee = new GetCrossedPassesUseCase(activityRepository, passRepository);

        List<MountainPass> result = testee.act(ACTIVITY_ID).mountainPasses();

        assertThat(result).extracting(MountainPass::name).containsExactly("Pass A", "Pass B");
    }

    @Test
    void act_uses50MeterDistanceThresholdForNearbyCoordinates() {
        Activity activity = Activity.builder()
                                    .id(ACTIVITY_ID)
                                    .title("test")
                                    .start(ZonedDateTime.now())
                                    .waypoints(List.of(
                                            // ~38m away from "Reference Pass" -> should match it
                                            waypoint(0, 47.1000, 11.1005),
                                            // >50m away from "Second Pass" -> should not match it
                                            waypoint(60, 47.1014, 11.1014)))
                                    .totals(new Activity.Totals(Duration.ZERO))
                                    .build();
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(ACTIVITY_ID, activity));
        ListBasedMountainPassRepository passRepository = new ListBasedMountainPassRepository(List.of(
                new MountainPass("Reference Pass", "AT", new LatLon(47.1000, 11.1000)),
                // both waypoints are >50m from this pass
                new MountainPass("Second Pass", "AT", new LatLon(47.1008, 11.1008))
                                                                                                    ));
        GetCrossedPassesUseCase testee = new GetCrossedPassesUseCase(activityRepository, passRepository);

        List<MountainPass> result = testee.act(ACTIVITY_ID).mountainPasses();

        assertThat(result).extracting(MountainPass::name).containsExactly("Reference Pass");
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
