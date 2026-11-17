package net.privactivity.store.model;

import net.privactivity.domain.Activity;
import net.privactivity.domain.None;
import net.privactivity.domain.Some;

public record DistanceFilter(int min, int max) implements Filter {

    @Override
    public boolean apply(Activity activity) {
        return switch (activity.totals().distanceInMeters()) {
            case None() -> false;
            case Some(var d) -> d >= min && d <= max;
        };
    }
}
