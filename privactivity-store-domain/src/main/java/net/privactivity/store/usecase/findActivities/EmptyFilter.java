package net.privactivity.store.usecase.findActivities;

import net.privactivity.domain.Activity;
import net.privactivity.store.model.Filter;

public class EmptyFilter implements Filter {
    @Override
    public boolean apply(Activity activity) {
        return true;
    }
}
