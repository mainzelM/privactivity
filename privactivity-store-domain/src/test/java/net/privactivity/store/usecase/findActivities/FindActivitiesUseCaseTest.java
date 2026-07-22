package net.privactivity.store.usecase.findActivities;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Activity.Totals;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.adapter.MetaData;
import net.privactivity.store.adapter.MetaDataRepository;
import net.privactivity.store.model.Filter;
import net.privactivity.store.usecase.gettiles.TileSummary;
import net.privactivity.store.usecase.townfinder.adapter.ActivityTownsRepository;
import net.privactivity.store.usecase.townfinder.model.Town;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;



import static net.privactivity.domain.Maybe.none;
import static net.privactivity.domain.Maybe.some;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindActivitiesUseCaseTest {

    @Mock
    ActivityRepository activityRepository;

    @Mock
    MetaDataRepository metaDataRepository;

    @Mock
    ActivityTownsRepository activityTownsRepository;

    FindActivitiesUseCase testee;

    @BeforeEach
    void setUp() {
        testee = new FindActivitiesUseCase(activityRepository, metaDataRepository, activityTownsRepository);
    }

    @Test
    void findActivities_shouldReturnActivitiesWithMetadata() {
        Activity activity = createActivity(1L, "Morning Run", 5000);
        when(activityRepository.getAll(false)).thenReturn(List.of(activity));

        MetaData metaData = new MetaData(some("My Awesome Run"));
        when(metaDataRepository.getMetaDataForActivity(activity.id())).thenReturn(Maybe.some(metaData));

        List<Activity> result = testee.findFilteredAndSortedActivities(false, new EmptyFilter(), null,
                                                                       SortDirection.asc);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("My Awesome Run");
        verify(activityRepository).getAll(false);
    }

    @Test
    void findFilteredActivities_shouldFilterByDistance() {
        Activity shortRun = createActivity(1L, "Short Run", 3000);
        Activity longRun = createActivity(2L, "Long Run", 10000);

        when(activityRepository.getAll(false)).thenReturn(List.of(shortRun, longRun));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        DistanceFilter filter = new DistanceFilter(4000, 12000);

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        assertThat(result).containsExactly(longRun);
    }

    @Test
    void findFilteredActivities_shouldHandleNoneDistance() {
        Activity noDistanceActivity = createActivityWithoutDistance(3L, "Unknown Distance");

        when(activityRepository.getAll(false)).thenReturn(List.of(noDistanceActivity));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        DistanceFilter filter = new DistanceFilter(0, 10000);

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        assertThat(result).isEmpty();
    }

    @Test
    void findFilteredActivities_shouldFilterByTileActivityIds() {
        Activity first = createActivity(1L, "First", 3000);
        Activity second = createActivity(2L, "Second", 5000);
        Activity third = createActivity(3L, "Third", 7000);

        when(activityRepository.getAll(false)).thenReturn(List.of(first, second, third));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        TileSummary tileSummary = new TileSummary("x0y0", null, null, null, 0, 0, null, null, List.of(2L, 3L));
        TileFilter filter = new TileFilter(tileSummary);

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        assertThat(result).containsExactly(second, third);
    }

    @Test
    void findFilteredActivities_shouldReturnEmptyListForTileWithoutActivityIds() {
        Activity first = createActivity(1L, "First", 3000);
        Activity second = createActivity(2L, "Second", 5000);

        when(activityRepository.getAll(false)).thenReturn(List.of(first, second));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        TileSummary tileSummary = new TileSummary("x0y0", null, null, null, 0, 0, null, null, List.of());
        TileFilter filter = new TileFilter(tileSummary);

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        assertThat(result).isEmpty();
    }

    @Test
    void getActivityById_shouldReturnActivityWithMetadata_TownsIntegration() {
        Activity activity = createActivity(10L, "Rennradfahren am Morgen", 50000);
        when(activityRepository.getActivityById(10L, true)).thenReturn(activity);

        MetaData metaData = new MetaData(none());
        when(metaDataRepository.getMetaDataForActivity(activity.id())).thenReturn(Maybe.some(metaData));
        when(activityTownsRepository.townsOfActivity(activity)).thenReturn(some(List.of(new Town("Berlin"),
                                                                                        new Town("Potsdam"))));

        Activity result = testee.getActivityById(10L, true);

        assertThat(result.title()).satisfiesAnyOf(
                title -> assertThat(title).isEqualTo("Berlin, Potsdam"),
                title -> assertThat(title).isEqualTo("Potsdam, Berlin"));
    }

    @Test
    void findFilteredActivities_shouldFilterByAscent() {
        Activity flatRide = createActivityWithAscent(1L, "Flat Ride", 100);
        Activity hillyRide = createActivityWithAscent(2L, "Hilly Ride", 1500);

        when(activityRepository.getAll(false)).thenReturn(List.of(flatRide, hillyRide));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        AscentFilter filter = new AscentFilter(1000, 2000);

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        assertThat(result).containsExactly(hillyRide);
    }

    @Test
    void findFilteredActivities_shouldFilterByAscentAndDistance() {
        Activity flatShort = createActivity(1L, "Flat Short", 3000, 100);
        Activity flatLong = createActivity(2L, "Flat Long", 15000, 100);
        Activity hillyShort = createActivity(3L, "Hilly Short", 3000, 1500);
        Activity hillyLong = createActivity(4L, "Hilly Long", 15000, 1500);

        when(activityRepository.getAll(false)).thenReturn(List.of(flatShort, flatLong, hillyShort, hillyLong));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        Filter distanceFilter = new DistanceFilter(10000, 20000);
        Filter ascentFilter = new AscentFilter(1000, 2000);
        AndFilter filter = new AndFilter(
                List.of(distanceFilter, ascentFilter));

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        assertThat(result).containsExactly(hillyLong);
    }

    @Test
    void findFilteredAndSortedActivities_shouldSortAscendingByDistance() {
        Activity longer = createActivity(1L, "Longer", 10000);
        Activity shorter = createActivity(2L, "Shorter", 3000);

        when(activityRepository.getAll(false)).thenReturn(List.of(longer, shorter));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        List<Activity> result = testee.findFilteredAndSortedActivities(false, new EmptyFilter(), "distance",
                                                                       SortDirection.asc);

        assertThat(result).containsExactly(shorter, longer);
    }

    @Test
    void findFilteredAndSortedActivities_shouldSortDescendingByDefault() {
        Activity shorter = createActivity(1L, "Shorter", 3000);
        Activity longer = createActivity(2L, "Longer", 10000);

        when(activityRepository.getAll(false)).thenReturn(List.of(shorter, longer));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        List<Activity> result = testee.findFilteredAndSortedActivities(false, new EmptyFilter(), "distance", null);

        assertThat(result).containsExactly(longer, shorter);
    }

    private Activity createActivity(long id, String title, int distanceMeters) {
        Totals totals = new Totals(
                some(distanceMeters), // distance
                none(), // ascent
                none(), // descent
                some(Duration.ofMinutes(30)), // movingTime
                none() // calories
        );

        return Activity.builder()
                       .id(id)
                       .title(title)
                       .start(ZonedDateTime.now())
                       .totals(totals)
                       .build();
    }

    private Activity createActivityWithoutDistance(long id, String title) {
        Totals totals = new Totals(Duration.ofMinutes(30));

        return Activity.builder()
                       .id(id)
                       .title(title)
                       .start(ZonedDateTime.now())
                       .totals(totals)
                       .build();
    }

    private Activity createActivityWithAscent(long id, String title, int ascent) {
        Totals totals = new Totals(
                none(), // distance
                some(ascent), // ascent
                none(), // descent
                some(Duration.ofMinutes(30)), // movingTime
                none() // calories
        );

        return Activity.builder()
                       .id(id)
                       .title(title)
                       .start(ZonedDateTime.now())
                       .totals(totals)
                       .build();
    }

    private Activity createActivity(long id, String title, int distanceMeters, int ascent) {
        Totals totals = new Totals(
                some(distanceMeters),
                some(ascent),
                none(),
                some(Duration.ofMinutes(30)),
                none());
        return Activity.builder()
                       .id(id)
                       .title(title)
                       .start(ZonedDateTime.now())
                       .totals(totals)
                       .build();
    }

    @Test
    void findFilteredActivities_shouldFilterByTitle() {
        Activity morningRun = createActivity(1L, "Morning Run", 5000);
        Activity eveningRide = createActivity(2L, "Evening Ride", 10000);
        Activity afternoonRun = createActivity(3L, "Afternoon Run", 7000);

        when(activityRepository.getAll(false)).thenReturn(List.of(morningRun, eveningRide, afternoonRun));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        TitleFilter filter = new TitleFilter("run");

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        assertThat(result).containsExactly(morningRun, afternoonRun);
    }

    @Test
    void findFilteredActivities_shouldFilterByTitleCaseInsensitive() {
        Activity activity = createActivity(1L, "MORNING RUN", 5000);

        when(activityRepository.getAll(false)).thenReturn(List.of(activity));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        TitleFilter filter = new TitleFilter("morning run");

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        assertThat(result).containsExactly(activity);
    }

    @Test
    void findFilteredActivities_shouldFilterByLocation() {
        // Berlin locations
        // Center: Brandenburg Gate (52.5162746, 13.3777041)
        double centerLat = 52.5162746;
        double centerLon = 13.3777041;
        int radiusMeters = 5000;

        // Inside: Victory Column (Siegessäule) (52.5145, 13.3501) - approx 2km away
        Activity inside = createActivityWithLocation(1L, "Inside Radius", 52.5145, 13.3501);

        // Outside: Charlottenburg Palace (52.5208, 13.2956) - approx 5.5km away
        Activity outside = createActivityWithLocation(2L, "Outside Radius", 52.5208, 13.2956);

        // Far Outside: Potsdam (52.3906, 13.0645) - approx 25km away
        Activity farOutside = createActivityWithLocation(3L, "Far Outside", 52.3906, 13.0645);

        when(activityRepository.getAll(false)).thenReturn(List.of(inside, outside, farOutside));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        // Stub getActivityById - only inside will pass the filter
        when(activityRepository.getActivityById(1L, true)).thenReturn(inside);

        LocationFilter filter = new LocationFilter(centerLat, centerLon,
                                                   radiusMeters);

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        assertThat(result).containsExactly(inside);
    }

    @Test
    void findFilteredActivities_shouldFilterByLocationBoundaryTest() {
        // Paris locations to verify 5km radius calculation
        // Center: Eiffel Tower (48.8584, 2.2945)
        double centerLat = 48.8584;
        double centerLon = 2.2945;
        int radiusMeters = 5000;

        // Just inside 5km: Arc de Triomphe (48.8738, 2.2950) - approx 1.7km away
        Activity justInside = createActivityWithLocation(1L, "Just Inside", 48.8738, 2.2950);

        // At boundary ~5km: Panthéon (48.8462, 2.3464) - approx 4.8km away
        Activity atBoundary = createActivityWithLocation(2L, "At Boundary", 48.8462, 2.3464);

        // Just outside 5km: La Défense (48.8922, 2.2358) - approx 5.2km away
        Activity justOutside = createActivityWithLocation(3L, "Just Outside", 48.8922, 2.2358);

        when(activityRepository.getAll(false)).thenReturn(List.of(justInside, atBoundary, justOutside));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());
        when(activityRepository.getActivityById(1L, true)).thenReturn(justInside);
        when(activityRepository.getActivityById(2L, true)).thenReturn(atBoundary);
        when(activityRepository.getActivityById(3L, true)).thenReturn(justOutside);

        LocationFilter filter = new LocationFilter(centerLat, centerLon,
                                                   radiusMeters);

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        assertThat(result).containsExactly(justInside, atBoundary);
    }

    @Test
    void findFilteredActivities_shouldHandleEmptyWaypoints() {
        Activity noWaypoints = Activity.builder()
                                       .id(1L)
                                       .title("No Waypoints")
                                       .start(ZonedDateTime.now())
                                       .waypoints(List.of())
                                       .totals(new Totals(Duration.ofMinutes(30)))
                                       .build();

        when(activityRepository.getAll(false)).thenReturn(List.of(noWaypoints));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());

        LocationFilter filter = new LocationFilter(52.5162746, 13.3777041,
                                                   5000);

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        assertThat(result).isEmpty();
    }

    @Test
    void findFilteredActivities_shouldFilterByLocationWithMultipleWaypoints() {
        // London locations
        // Center: Trafalgar Square (51.5081, -0.1281)
        double centerLat = 51.5081;
        double centerLon = -0.1281;
        int radiusMeters = 3000;

        // Activity that passes through the area - some waypoints inside, some outside
        LatLon insidePoint = new LatLon(51.5074, -0.1278); // Very close to center
        LatLon outsidePoint = new LatLon(51.5355, -0.1014); // King's Cross - approx 4km away

        Waypoint wp1 = new Waypoint(0, Maybe.some(insidePoint), Maybe.none(), Maybe.none(), Maybe.none(),
                                    Maybe.none(), Maybe.none(), Maybe.none(), 1);
        Waypoint wp2 = new Waypoint(60, Maybe.some(outsidePoint), Maybe.none(), Maybe.none(), Maybe.none(),
                                    Maybe.none(), Maybe.none(), Maybe.none(), 1);

        Activity mixed = Activity.builder()
                                 .id(1L)
                                 .title("Mixed Route")
                                 .start(ZonedDateTime.now())
                                 .waypoints(List.of(wp1, wp2))
                                 .totals(new Totals(Duration.ofMinutes(30)))
                                 .build();

        Activity fullyOutside = createActivityWithLocation(2L, "Fully Outside", 51.5355, -0.1014);

        when(activityRepository.getAll(false)).thenReturn(List.of(mixed, fullyOutside));
        when(metaDataRepository.getMetaDataForActivity(anyLong())).thenReturn(Maybe.none());
        when(activityRepository.getActivityById(1L, true)).thenReturn(mixed);
        when(activityRepository.getActivityById(2L, true)).thenReturn(fullyOutside);

        LocationFilter filter = new LocationFilter(centerLat, centerLon,
                                                   radiusMeters);

        List<Activity> result = testee.findFilteredAndSortedActivities(false, filter, null, SortDirection.asc);

        // Should match mixed because at least one waypoint is inside the radius
        assertThat(result).containsExactly(mixed);
    }

    private Activity createActivityWithLocation(long id, String title, double lat, double lon) {
        LatLon latLon = new LatLon(lat, lon);
        Waypoint wp = new Waypoint(0, Maybe.some(latLon), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(),
                                   Maybe.none(), Maybe.none(), 1);
        List<Waypoint> waypoints = List.of(wp);
        // Bounds will be calculated from waypoints
        return Activity.builder()
                       .id(id)
                       .title(title)
                       .start(ZonedDateTime.now())
                       .waypoints(waypoints)
                       .totals(new Totals(Duration.ofMinutes(30)))
                       .build();
    }
}
