package net.privactivity.store.usecase.findActivities;

import net.privactivity.domain.Activity;
import net.privactivity.domain.None;
import net.privactivity.domain.Some;
import net.privactivity.store.model.Filter;

public record DistanceFilter(int min, int max) implements Filter {

    @Override
    public boolean apply(Activity activity) {
        return switch (activity.totals().distanceInMeters()) {
            case None() -> false;
            case Some(var d) -> d >= min && d <= max;
        };
    }
}
