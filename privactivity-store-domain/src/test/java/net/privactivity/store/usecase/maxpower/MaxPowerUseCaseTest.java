package net.privactivity.store.usecase.maxpower;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Bounds;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.model.MapBasedActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import static net.privactivity.domain.Maybe.none;
import static net.privactivity.domain.Maybe.some;
import static org.assertj.core.api.Assertions.assertThat;

class MaxPowerUseCaseTest {

    private MaxPowerUseCase testee;

    @BeforeEach
    void setUp() {
        testee = new MaxPowerUseCase(new MapBasedActivityRepository(Map.of()), 1, 5, 10, 15, 20);
    }

    @Test
    void maxPowerAll100() {
        Activity a = act(Map.of(70, 100, 140, 100));
        MaxPower maxPower = testee.maxPowerOne(a);

        assertThat(maxPower.getForMinutes(1).orElseThrow().watts()).isEqualTo(100);
        assertThat(maxPower.getForMinutes(5)).isEmpty();
    }

    @Test
    void maxPowerMissingIgnored() {
        Map<Integer, Integer> m = new HashMap<>(Map.of(150, 100, 300, 200));
        m.put(140, null);
        Activity a = act(m);
        MaxPower maxPower = testee.maxPowerOne(a);

        assertThat(maxPower.getForMinutes(1).orElseThrow().watts()).isEqualTo(200);
        assertThat(maxPower.getForMinutes(5).orElseThrow().watts()).isEqualTo(150);
    }

    @Test
    void maxPower100then200() {
        Activity a = act(Map.of(150, 100, 300, 200));
        MaxPower maxPower = testee.maxPowerOne(a);

        assertThat(maxPower.getForMinutes(1).orElseThrow().watts()).isEqualTo(200);
        assertThat(maxPower.getForMinutes(5).orElseThrow().watts()).isEqualTo(150);
    }

    @Test
    void maxPower100then200_for_30() {
        Activity a = act(Map.of(30, 100, 60, 200, 90, 300, 300, 100));
        MaxPower maxPower = testee.maxPowerOne(a);

        assertThat(maxPower.getForMinutes(1).orElseThrow().watts()).isEqualTo(250);
        assertThat(maxPower.getForMinutes(5).orElseThrow().watts()).isEqualTo(130);
    }

    private static ZonedDateTime createZDT(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 12, 45, 59, 1234, ZoneId.of("UTC+1"));
    }

    private Activity act(Map<Integer, Integer> secondsToPower) {
        return new Activity(1, "act ", createZDT(2024, 4, 30),
                            secondsToPower.entrySet().stream()
                                          .sorted(Map.Entry.comparingByKey())
                                          .map(e -> new Waypoint(e.getKey(), Maybe.none(), Maybe.none(),
                                                                 Maybe.nullAsNone(e.getValue()), Maybe.none(),
                                                                 Maybe.none(), Maybe.none(), Maybe.none(), 1))
                                          .toList(),
                            new Activity.Totals(none(),
                                                none(),
                                                none(),
                                                some(Duration.of(100, ChronoUnit.MINUTES)),
                                                none()),
                            new Activity.Averages(none(), none(), none(), none()),
                            new Activity.Maxima(none(), none(), none(), none()),
                            List.of(),
                            Bounds.of(0d, 0d, 0d, 0d));
    }
}