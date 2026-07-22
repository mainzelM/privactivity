package net.privactivity.store.usecase.climbdetector;

import net.privactivity.domain.Activity;
import net.privactivity.domain.ClimbPointer;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.model.MapBasedActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



import static org.assertj.core.api.Assertions.assertThat;

class ClimbDetectorUseCaseTest {

    private static final long ACTIVITY_ID = 1L;

    private ClimbDetectorUseCase testee;

    @BeforeEach
    void setUp() {
        testee = new ClimbDetectorUseCase(new MapBasedActivityRepository(Map.of()), 100, 20);
    }

    @Test
    void emptyInputReturnsNoClimbs() {
        testee = testeeWith();
        assertThat(testee.detect(ACTIVITY_ID)).isEmpty();
    }

    @Test
    void flatRouteReturnsNoClimbs() {
        testee = testeeWith(100, 100, 100, 100, 100);

        assertThat(testee.detect(ACTIVITY_ID)).isEmpty();
    }

    @Test
    void descendingRouteReturnsNoClimbs() {
        testee = testeeWith(500, 400, 300, 200, 100);

        assertThat(testee.detect(ACTIVITY_ID)).isEmpty();
    }

    @Test
    void singleClimbDetected() {
        testee = testeeWith(100, 150, 200, 250, 300, 200);

        List<ClimbPointer> climbs = testee.detect(ACTIVITY_ID);

        assertThat(climbs).hasSize(1);
        assertThat(climbs.get(0).ascent()).isEqualTo(200);
        assertThat(climbs.get(0).startMeters()).isEqualTo(0);
        assertThat(climbs.get(0).endMeters()).isEqualTo(400); // ends at the peak (distanceInMeter=400)
    }

    @Test
    void climbBelowMinAscentIsIgnored() {
        // Only 50m ascent: below minClimbAscent=100
        testee = testeeWith(100, 120, 140, 150, 100);

        assertThat(testee.detect(ACTIVITY_ID)).isEmpty();
    }

    @Test
    void smallDescentWithinClimbIsAllowed() {
        // Climb with a small dip in the middle (within 20% budget)
        // ascent of 200, dip of 30 (15% of 200) → allowed
        testee = testeeWith(100, 200, 300, 270, 300, 250);

        assertThat(testee.detect(ACTIVITY_ID)).hasSize(1);
    }

    @Test
    void largeDescentTerminatesClimb() {
        // First climb: 100→300 (200m ascent), then drops 150m (75% of 200 > 20% budget) → climb ends
        // Second segment: 150→200 (50m) → below minClimb, ignored
        testee = testeeWith(100, 200, 300, 150, 200);

        List<ClimbPointer> climbs = testee.detect(ACTIVITY_ID);

        assertThat(climbs).hasSize(1);
        assertThat(climbs.get(0).ascent()).isEqualTo(200);
    }

    @Test
    void overlappingClimbsRetainsLargerAscent() {
        testee = testeeWith(100, 200, 300, 400, 250);

        List<ClimbPointer> climbs = testee.detect(ACTIVITY_ID);

        assertThat(climbs).hasSize(1);
        assertThat(climbs.get(0).ascent()).isEqualTo(300);
    }

    @Test
    void tailAfterPeakIsRemoved() {
        testee = testeeWith(100, 200, 300, 250, 200);

        List<ClimbPointer> climbs = testee.detect(ACTIVITY_ID);

        assertThat(climbs).hasSize(1);
        // endMeters should be at the peak (index 2 → distanceInMeter = 200)
        assertThat(climbs.get(0).endMeters()).isEqualTo(200);
    }

    @Test
    void twoSeparateClimbsDetected() {
        // Climb 1: 100→400 (300m), descent to 50 (350m drop >> 20% of 300), Climb 2: 50→300 (250m)
        testee = testeeWith(100, 200, 300, 400, 50, 150, 250, 300, 200);

        List<ClimbPointer> climbs = testee.detect(ACTIVITY_ID);

        assertThat(climbs).hasSize(2);
        assertThat(climbs.get(0).startMeters()).isLessThan(climbs.get(1).startMeters());
    }

    @Test
    void climbsAreSortedByStartDistance() {
        testee = testeeWith(100, 200, 300, 400, 50, 100, 200, 300, 200);

        List<ClimbPointer> climbs = testee.detect(ACTIVITY_ID);

        for (int i = 0; i < climbs.size() - 1; i++) {
            assertThat(climbs.get(i).startMeters()).isLessThanOrEqualTo(climbs.get(i + 1).startMeters());
        }
    }

    /**
     * Creates a testee backed by an activity with the given altitude profile.
     */
    private ClimbDetectorUseCase testeeWith(int... altitudes) {
        Activity activity = new Activity(ACTIVITY_ID, "test", null, wps(altitudes), java.time.Duration.ZERO);
        return new ClimbDetectorUseCase(new MapBasedActivityRepository(Map.of(ACTIVITY_ID, activity)), 100, 20);
    }

    /**
     * Creates waypoints from altitude values, with distanceInMeter = index * 100.
     */
    private static List<Waypoint> wps(int... altitudes) {
        List<Waypoint> result = new ArrayList<>();
        for (int i = 0; i < altitudes.length; i++) {
            result.add(new Waypoint(i * 10, Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(),
                                    Maybe.some(altitudes[i]), Maybe.some(i * 100), 1));
        }
        return result;
    }
}
