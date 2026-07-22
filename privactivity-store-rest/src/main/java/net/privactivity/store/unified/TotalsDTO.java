package net.privactivity.store.unified;

import net.privactivity.domain.Activity;
import java.time.Duration;

public record TotalsDTO(double distance, double duration, int ascent, int descent) {
    public TotalsDTO(Activity.Totals totals) {
        this(totals.distanceInMeters().orElse(0),
             totals.movingTime().orElse(Duration.ZERO).toSeconds(),
             totals.ascent().orElse(0),
             totals.descent().orElse(0));
    }
}