package net.privactivity.store.usecase.findActivities;

import net.privactivity.domain.Activity;
import net.privactivity.store.model.Filter;

public record TitleFilter(String searchTerm) implements Filter {

    @Override
    public boolean apply(Activity activity) {
        if (activity.title() == null) {
            return false;
        }
        return activity.title().toLowerCase().contains(searchTerm.toLowerCase());
    }
}
