package net.privactivity.store.usecase.rebuildtiles;

import net.privactivity.domain.Activity;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.model.MapBasedActivityRepository;
import net.privactivity.store.usecase.gettiles.TileSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;



import static net.privactivity.domain.Maybe.none;
import static net.privactivity.domain.Maybe.some;
import static org.assertj.core.api.Assertions.assertThat;

class RebuildTilesSnapshotUseCaseTest {
    private RebuildTilesSnapshotUseCase testee;
    private MapBasedActivityRepository activityRepository;

    @BeforeEach
    void setUp() {
        Map<Long, Activity> activityMap = Map.of(
                1L, activity(1L, ZonedDateTime.parse("2026-04-01T10:15:00Z"),
                             List.of(
                                     waypoint(48.10, 11.50, 120, 140),
                                     waypoint(48.11, 11.51, 180, 150),
                                     waypoint(48.31, 11.71, 300, 170))),
                2L, activity(2L, ZonedDateTime.parse("2026-04-03T10:15:00Z"),
                             List.of(
                                     waypoint(48.10, 11.50, 200, 160),
                                     waypoint(48.31, 11.71, 90, 110)))
                                                );
        activityRepository = new MapBasedActivityRepository(activityMap);
        testee = new RebuildTilesSnapshotUseCase(activityRepository,
                                                 new TilesProperties(0.2, 0.2),
                                                 null);
    }

    @Test
    void buildSnapshot_shouldComputeMetricsFromWaypointsWithinTile() {
        TileSnapshot result = testee.buildSnapshot(activityRepository.getAll(false));

        assertThat(result.tiles()).hasSize(2);

        TileSummary sharedTile = result.tiles().stream()
                                       .filter(tile -> tile.activityCount() == 2)
                                       .findFirst()
                                       .orElseThrow();

        assertThat(sharedTile.firstActivityId()).isEqualTo(1L);
        assertThat(sharedTile.latestActivityId()).isEqualTo(2L);
        assertThat(sharedTile.maxPower()).isEqualTo(200);
        assertThat(sharedTile.maxHeartRate()).isEqualTo(160);
        assertThat(sharedTile.averagePower()).isEqualTo((120d + 180d + 200d) / 3d);
        assertThat(sharedTile.averageHeartRate()).isEqualTo((140d + 150d + 160d) / 3d);
        assertThat(sharedTile.activityIds()).containsExactly(1L, 2L);

        TileSummary secondTile = result.tiles().stream()
                                       .filter(tile -> tile.activityCount() == 2)
                                       .filter(tile -> !tile.tileId().equals(sharedTile.tileId()))
                                       .findFirst()
                                       .orElseThrow();

        assertThat(secondTile.maxPower()).isEqualTo(300);
        assertThat(secondTile.maxHeartRate()).isEqualTo(170);
        assertThat(secondTile.averagePower()).isEqualTo((300d + 90d) / 2d);
        assertThat(secondTile.averageHeartRate()).isEqualTo((170d + 110d) / 2d);
        assertThat(secondTile.activityIds()).containsExactly(1L, 2L);
    }

    private static Activity activity(long id,
                                     ZonedDateTime start,
                                     List<Waypoint> waypoints) {
        return Activity.builder()
                       .id(id)
                       .title("Activity " + id)
                       .start(start)
                       .waypoints(waypoints)
                       .totals(new Activity.Totals(some(1000), none(), none(), some(Duration.ofMinutes(30)),
                                                   none()))
                       .averages(new Activity.Averages())
                       .maxima(new Activity.Maxima())
                       .build();
    }

    private static Waypoint waypoint(double lat, double lon, Integer power, Integer heartRate) {
        return new Waypoint(0, Maybe.some(new LatLon(lat, lon)), Maybe.none(), power != null ? Maybe.some(power) :
                                                                               Maybe.none(), heartRate != null ?
                                                                                             Maybe.some(heartRate) :
                                                                                             Maybe.none(),
                            Maybe.none(), Maybe.none(), Maybe.none(), 1);
    }

}