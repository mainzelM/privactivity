package net.privactivity.store.model;

import net.privactivity.domain.Activity;

public record TitleFilter(String searchTerm) implements Filter {

    @Override
    public boolean apply(Activity activity) {
        if (activity.title() == null) {
            return false;
        }
        return activity.title().toLowerCase().contains(searchTerm.toLowerCase());
    }
}
