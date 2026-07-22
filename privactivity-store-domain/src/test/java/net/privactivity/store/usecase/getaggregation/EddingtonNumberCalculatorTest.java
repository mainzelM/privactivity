package net.privactivity.store.usecase.getaggregation;

import net.privactivity.domain.Activity;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;



import static net.privactivity.domain.Maybe.none;
import static net.privactivity.domain.Maybe.some;
import static org.assertj.core.api.Assertions.assertThat;

class EddingtonNumberCalculatorTest {

    @Test
    void test_empty_activities() {
        EddingtonNumberCalculator testee = new EddingtonNumberCalculator();
        assertThat(testee.calculate(List.of())).isEqualTo(0);
    }

    @Test
    void test_single_activity() {
        EddingtonNumberCalculator testee = new EddingtonNumberCalculator();
        Activity activity = createActivity(1, 1, 10000);
        assertThat(testee.calculate(List.of(activity))).isEqualTo(1);
    }

    @Test
    void test_activities_with_no_distance() {
        EddingtonNumberCalculator testee = new EddingtonNumberCalculator();
        Activity activity = createActivity(1, 1, 0);
        assertThat(testee.calculate(List.of(activity))).isEqualTo(0);
    }

    @Test
    void test_from_6_activities() {
        EddingtonNumberCalculator testee = new EddingtonNumberCalculator();

        Activity activity1 = createActivity(1, 1, 1000);
        Activity activity2 = createActivity(2, 2, 6000);
        Activity activity3 = createActivity(3, 3, 3000);
        Activity activity4 = createActivity(4, 4, 4000);
        Activity activity5 = createActivity(5, 5, 5000);
        Activity activity6 = createActivity(6, 6, 2000);

        assertThat(testee.calculate(List.of(activity1, activity2, activity3, activity4, activity5,
                                            activity6))).isEqualTo(3);
    }


    @Test
    void test_activities_with_same_day_multiple_activities() {
        EddingtonNumberCalculator testee = new EddingtonNumberCalculator();
        Activity activity1 = createActivity(1, 1, 1000);
        Activity activity2 = createActivity(2, 1, 1000);
        Activity activity3 = createActivity(3, 2, 2000);

        assertThat(testee.calculate(List.of(activity1, activity2, activity3))).isEqualTo(2);
    }

    private Activity createActivity(long id, int day, int meters) {
        ZonedDateTime start = ZonedDateTime.of(2024, 1, day, 12, 45, 59, 1234, ZoneId.of("UTC+1"));
        return new Activity(id, "act " + id, start,
                            List.of(),
                            new Activity.Totals(some(meters),
                                                none(),
                                                none(),
                                                some(Duration.of(10, ChronoUnit.MINUTES)),
                                                none()),
                            new Activity.Averages(none(), none(), none(), none()),
                            new Activity.Maxima(none(), none(), none(), none()),
                            List.of(),
                            null);
    }
}