package net.privactivity.store.service;

import net.privactivity.domain.Activity;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.usecase.townfinder.adapter.ActivityTownsRepository;
import net.privactivity.store.usecase.townfinder.adapter.TownFinder;
import net.privactivity.store.usecase.townfinder.addtowns.AddMissingTownsToActivitiesUseCase;
import net.privactivity.store.usecase.townfinder.addtowns.model.AddedAndExistingTowns;
import net.privactivity.store.usecase.townfinder.findmaintowns.FindActivityMainTownsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TownFinderService {

    private static final Logger logger = LoggerFactory.getLogger(TownFinderService.class);

    private final ActivityRepository activityRepository;
    private final TownFinder townFinder;
    private final ActivityTownsRepository activityTownsRepository;

    @Autowired
    public TownFinderService(ActivityRepository activityRepository, ActivityTownsRepository activityTownsRepository,
                             TownFinder townFinder) {
        this.activityRepository = activityRepository;
        this.activityTownsRepository = activityTownsRepository;
        this.townFinder = townFinder;
    }

    public List<String> findAllTowns(long activityId) {
        Activity activity = activityRepository.getActivityById(activityId, true);
        FindActivityMainTownsUseCase findActivityMainTownsUseCase = new FindActivityMainTownsUseCase(townFinder);

        return findActivityMainTownsUseCase.act(activity);
    }

    public List<String> findCornerTowns(long activityId) {
        Activity activity = activityRepository.getActivityById(activityId, true);
        FindActivityMainTownsUseCase findActivityMainTownsUseCase = new FindActivityMainTownsUseCase(townFinder);

        return findActivityMainTownsUseCase.findMainTownsOnCorners(activity);
    }

    public List<String> findBoundsTowns(long activityId) {
        Activity activity = activityRepository.getActivityById(activityId, true);
        FindActivityMainTownsUseCase findActivityMainTownsUseCase = new FindActivityMainTownsUseCase(townFinder);

        return findActivityMainTownsUseCase.findMainTownsOnBounds(activity);
    }

    public AddedAndExistingTowns addMissingTowns() {
        logger.info("addMissingTowns");
        List<Activity> allActivities = activityRepository.getAll(false);
        logger.info("loaded activities: {}", allActivities.size());
        AddMissingTownsToActivitiesUseCase addMissingTownsToActivitiesUseCase =
                new AddMissingTownsToActivitiesUseCase(activityTownsRepository, townFinder);
        return addMissingTownsToActivitiesUseCase.addMissingTownsToActivities(allActivities);
    }
}
