package net.privactivity.store.usecase.findActivities;

import net.privactivity.domain.Activity;
import net.privactivity.store.model.Filter;

import java.time.DayOfWeek;

public record DayOfWeekFilter(DayOfWeek dayOfWeek) implements Filter {

    @Override
    public boolean apply(Activity activity) {
        return activity.getDayOfWeek() == dayOfWeek;
    }
}
