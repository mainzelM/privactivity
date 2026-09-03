package net.privactivity.store.usecase.allcrossedpasses;

import net.privactivity.domain.Activity;
import net.privactivity.domain.LatLon;
import net.privactivity.store.usecase.crossedpasses.MountainPass;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AllCrossedPassesTest {

    private static final MountainPass PASS = new MountainPass("Pass A", "AT", new LatLon(47.1000, 11.1000));

    @Test
    void noteCrossing_addsNewPassCrossingWhenPassNotYetKnown() {
        AllCrossedPasses testee = new AllCrossedPasses(List.of());
        Activity activity = activityStartingAt(1);

        AllCrossedPasses result = testee.noteCrossing(activity, PASS);

        assertThat(result.allPassCrossings()).containsExactly(new PassCrossing(PASS, activity, activity));
    }

    @Test
    void noteCrossing_replacesFirstCrossingWhenActivityStartsBeforeExistingFirstCrossing() {
        Activity existingFirst = activityStartingAt(5);
        Activity existingLast = activityStartingAt(10);
        AllCrossedPasses testee = new AllCrossedPasses(List.of(new PassCrossing(PASS, existingFirst, existingLast)));
        Activity earlierActivity = activityStartingAt(1);

        AllCrossedPasses result = testee.noteCrossing(earlierActivity, PASS);

        assertThat(result.allPassCrossings()).containsExactly(new PassCrossing(PASS, earlierActivity, existingLast));
    }

    @Test
    void noteCrossing_replacesLastCrossingWhenActivityStartsAfterExistingLastCrossing() {
        Activity existingFirst = activityStartingAt(5);
        Activity existingLast = activityStartingAt(10);
        AllCrossedPasses testee = new AllCrossedPasses(List.of(new PassCrossing(PASS, existingFirst, existingLast)));
        Activity laterActivity = activityStartingAt(15);

        AllCrossedPasses result = testee.noteCrossing(laterActivity, PASS);

        assertThat(result.allPassCrossings()).containsExactly(new PassCrossing(PASS, existingFirst, laterActivity));
    }

    @Test
    void noteCrossing_returnsUnchangedWhenActivityStartsBetweenExistingFirstAndLastCrossing() {
        Activity existingFirst = activityStartingAt(5);
        Activity existingLast = activityStartingAt(10);
        AllCrossedPasses testee = new AllCrossedPasses(List.of(new PassCrossing(PASS, existingFirst, existingLast)));
        Activity inBetweenActivity = activityStartingAt(7);

        AllCrossedPasses result = testee.noteCrossing(inBetweenActivity, PASS);

        assertThat(result).isSameAs(testee);
    }

    @Test
    void noteCrossing_addsSeparatePassCrossingForDifferentMountainPass() {
        MountainPass otherPass = new MountainPass("Pass B", "AT", new LatLon(47.2000, 11.2000));
        Activity existingActivity = activityStartingAt(5);
        AllCrossedPasses testee = new AllCrossedPasses(List.of(new PassCrossing(PASS, existingActivity, existingActivity)));
        Activity newActivity = activityStartingAt(1);

        AllCrossedPasses result = testee.noteCrossing(newActivity, otherPass);

        assertThat(result.allPassCrossings()).containsExactlyInAnyOrder(
                new PassCrossing(PASS, existingActivity, existingActivity),
                new PassCrossing(otherPass, newActivity, newActivity));
    }

    private Activity activityStartingAt(int dayOfMonth) {
        return Activity.builder()
                       .id(dayOfMonth)
                       .title("test")
                       .start(ZonedDateTime.parse("2024-01-" + String.format("%02d", dayOfMonth) + "T10:00:00Z"))
                       .waypoints(List.of())
                       .totals(new Activity.Totals(Duration.ZERO))
                       .build();
    }
}
