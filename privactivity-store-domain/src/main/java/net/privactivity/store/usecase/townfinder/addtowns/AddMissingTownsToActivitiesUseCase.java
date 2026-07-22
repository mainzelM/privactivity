package net.privactivity.store.usecase.townfinder.addtowns;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Bounds;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.None;
import net.privactivity.domain.Some;
import net.privactivity.store.usecase.townfinder.adapter.ActivityTownsRepository;
import net.privactivity.store.usecase.townfinder.adapter.TownFinder;
import net.privactivity.store.usecase.townfinder.addtowns.model.AddedAndExistingTowns;
import net.privactivity.store.usecase.townfinder.model.Town;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.List;

public class AddMissingTownsToActivitiesUseCase {

    private static final Logger logger = LoggerFactory.getLogger(AddMissingTownsToActivitiesUseCase.class);

    private final ActivityTownsRepository activityTownsRepository;
    private final TownFinder townFinder;

    public AddMissingTownsToActivitiesUseCase(ActivityTownsRepository activityTownsRepository, TownFinder townFinder) {
        this.activityTownsRepository = activityTownsRepository;
        this.townFinder = townFinder;
    }

    public AddedAndExistingTowns addMissingTownsToActivities(Collection<Activity> activities) {
        int existing = 0;
        int added = 0;
        for (Activity activity : activities) {
            if (addMissingTownsToActivity(activity)) {
                added++;
            } else {
                existing++;
            }
        }
        return new AddedAndExistingTowns(added, existing);
    }


    private boolean addMissingTownsToActivity(Activity activity) {
        Maybe<List<Town>> towns = activityTownsRepository.townsOfActivity(activity);
        switch (towns) {
            case None() -> {
                computeAndStoreTowns(activity);
                return true;
            }
            case Some<List<Town>> _ -> {
                return false;
            }
        }
    }

    private void computeAndStoreTowns(Activity activity) {
        logger.info("Adding missing towns to activity {}", activity.id());
        List<String> mainTowns = findMainTownsFromBounds(activity);
        activityTownsRepository.setActivityTowns(activity, mainTowns);
    }

    List<String> findMainTownsFromBounds(Activity activity) {
        Bounds bounds = activity.bounds();
        return townFinder.findNearbyTownsFromLatLons(List.of(bounds.northernmostLatLon(), bounds.southernmostLatLon(),
                                                             bounds.easternmostLatLon(), bounds.westernmostLatLon()));
    }
}
