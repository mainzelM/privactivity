package net.privactivity.store.model;

import net.privactivity.domain.Activity;

public interface Filter {
    boolean apply(Activity activity);
}
