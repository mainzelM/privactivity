package net.privactivity.store.model;

import net.privactivity.domain.Activity;
import net.privactivity.domain.None;
import net.privactivity.domain.Some;

public record AscentFilter(int min, int max) implements Filter {

    @Override
    public boolean apply(Activity activity) {
        return switch (activity.totals().ascent()) {
            case None() -> false;
            case Some(var a) -> a >= min && a <= max;
        };
    }
}
