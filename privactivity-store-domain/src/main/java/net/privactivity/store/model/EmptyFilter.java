package net.privactivity.store.model;

import net.privactivity.domain.Activity;

public class EmptyFilter implements Filter {
    @Override
    public boolean apply(Activity activity) {
        return true;
    }
}
