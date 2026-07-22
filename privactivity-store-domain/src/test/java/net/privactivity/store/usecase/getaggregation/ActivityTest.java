package net.privactivity.store.usecase.getaggregation;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Bounds;
import net.privactivity.store.usecase.getaggregation.model.Year;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;



import static net.privactivity.domain.Maybe.none;
import static net.privactivity.domain.Maybe.some;

public class ActivityTest {
    protected final Activity a1 = act(1, 2024, 1, 1, 15, 150);
    protected final Activity a2 = act(2, 2024, 2, 1, 27, 270);
    protected final Activity a3 = act(3, 2026, 1, 1, 30, 300);
    protected final Activity a4 = act(4, 2026, 5, 11, 41, 410);
    protected final Activity a5 = act(5, 2027, 1, 1, 55, 550);
    protected final Activity a6 = act(6, 2027, 6, 10, 66, 660);
    protected final Activity a7 = act(7, 2027, 6, 11, 77, 770);
    protected final Year y2024 = new Year(2024);
    protected final Year y2026 = new Year(2026);
    protected final Year y2027 = new Year(2027);
    private final Year y2025 = new Year(2025);

    private static ZonedDateTime createZDT(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 12, 45, 59, 1234, ZoneId.of("UTC+1"));
    }

    private Activity act(long id, int year, int month, int day, int km, int durationInMinutes) {
        return new Activity(id, "act " + id, ActivityTest.createZDT(year, month, day),
                            List.of(),
                            new Activity.Totals(some(km * 1000),
                                                none(),
                                                none(),
                                                some(Duration.of(durationInMinutes, ChronoUnit.MINUTES)),
                                                none()),
                            new Activity.Averages(none(), none(), none(), none()),
                            new Activity.Maxima(none(), none(), none(), none()),
                            List.of(),
                            Bounds.of(0d, 0d, 0d, 0d));
    }
}
