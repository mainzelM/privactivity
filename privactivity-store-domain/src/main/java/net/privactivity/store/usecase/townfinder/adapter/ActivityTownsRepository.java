package net.privactivity.store.usecase.townfinder.adapter;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Maybe;
import net.privactivity.store.usecase.townfinder.model.Town;
import java.util.List;

public interface ActivityTownsRepository {
    Maybe<List<Town>> townsOfActivity(Activity activity);

    void setActivityTowns(Activity activity, List<String> mainTowns);
}
