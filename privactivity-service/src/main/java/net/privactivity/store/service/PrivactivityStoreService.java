package net.privactivity.store.service;

import net.privactivity.domain.Activity;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.adapter.MetaDataRepository;
import net.privactivity.store.model.Filter;
import net.privactivity.store.model.ReadActivitiesDomainService;
import net.privactivity.store.usecase.changetitle.ChangeTitleUseCase;
import net.privactivity.store.usecase.findActivities.FindActivitiesUseCase;
import net.privactivity.store.model.SortDirection;
import net.privactivity.store.usecase.townfinder.adapter.ActivityTownsRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PrivactivityStoreService {

    private final ActivityRepository activityRepository;
    private final MetaDataRepository metaDataRepository;
    private final ActivityTownsRepository activityTownsRepository;

    public PrivactivityStoreService(ActivityRepository activityRepository,
                                    MetaDataRepository metaDataRepository,
                                    ActivityTownsRepository activityTownsRepository) {
        this.activityRepository = activityRepository;
        this.metaDataRepository = metaDataRepository;
        this.activityTownsRepository = activityTownsRepository;
    }

    public List<Activity> getAllActivities(boolean includeWaypoints, Filter filter,
                                           String sortBy, SortDirection sortDirection) {
        FindActivitiesUseCase useCase = createFindActivitiesUseCase();
        return useCase.findFilteredAndSortedActivities(includeWaypoints, filter, sortBy, sortDirection);
    }

    private FindActivitiesUseCase createFindActivitiesUseCase() {
        return new FindActivitiesUseCase(new ReadActivitiesDomainService(activityRepository, metaDataRepository, activityTownsRepository));
    }

    public Activity getActivity(long activityId, boolean includeWaypoints) {
        FindActivitiesUseCase useCase =createFindActivitiesUseCase();
        return useCase.getActivityById(activityId, includeWaypoints);
    }

    public void changeTitle(long activityId, String newTitle) {
        ChangeTitleUseCase useCase = new ChangeTitleUseCase(metaDataRepository);
        useCase.changeTitle(activityId, newTitle);
    }
}
