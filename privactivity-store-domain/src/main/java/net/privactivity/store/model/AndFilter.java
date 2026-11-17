package net.privactivity.store.model;

import net.privactivity.domain.Activity;

import java.util.List;

public record AndFilter(List<Filter> filters) implements Filter {

    @Override
    public boolean apply(Activity activity) {
        return filters.stream()
                      .allMatch(f -> f.apply(activity));
    }
}
