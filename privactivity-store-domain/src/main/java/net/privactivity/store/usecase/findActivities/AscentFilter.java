package net.privactivity.store.usecase.findActivities;

import net.privactivity.domain.Activity;
import net.privactivity.domain.None;
import net.privactivity.domain.Some;
import net.privactivity.store.model.Filter;

public record AscentFilter(int min, int max) implements Filter {

    @Override
    public boolean apply(Activity activity) {
        return switch (activity.totals().ascent()) {
            case None() -> false;
            case Some(var a) -> a >= min && a <= max;
        };
    }
}
