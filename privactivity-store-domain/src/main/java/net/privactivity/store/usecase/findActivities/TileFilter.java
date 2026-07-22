package net.privactivity.store.usecase.findActivities;

import net.privactivity.domain.Activity;
import net.privactivity.store.model.Filter;
import net.privactivity.store.usecase.gettiles.TileSummary;

public class TileFilter implements Filter {
    private final TileSummary tileSummary;

    public TileFilter(TileSummary tileSummary) {
        this.tileSummary = tileSummary;
    }

    @Override
    public boolean apply(Activity activity) {
        return tileSummary.activityIds().contains(activity.id());
    }
}
