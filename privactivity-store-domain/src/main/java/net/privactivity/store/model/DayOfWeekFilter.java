package net.privactivity.store.model;

import net.privactivity.domain.Activity;

import java.time.DayOfWeek;

public record DayOfWeekFilter(DayOfWeek dayOfWeek) implements Filter {

    @Override
    public boolean apply(Activity activity) {
        return activity.getDayOfWeek() == dayOfWeek;
    }
}
