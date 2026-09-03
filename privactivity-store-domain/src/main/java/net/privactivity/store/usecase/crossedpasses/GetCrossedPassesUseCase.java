package net.privactivity.store.usecase.crossedpasses;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.adapter.MountainPassRepository;
import java.util.List;
import java.util.Optional;

public class GetCrossedPassesUseCase {
    private final ActivityRepository activityRepository;
    private final List<MountainPass> mountainPasses;

    public GetCrossedPassesUseCase(ActivityRepository activityRepository,
                                   MountainPassRepository mountainPassRepository) {
        this.activityRepository = activityRepository;
        this.mountainPasses = mountainPassRepository.getAllPasses();
    }

    public CrossedPasses act(long activityId) {
        Activity activity = activityRepository.getActivityById(activityId, true);
        List<MountainPass> crossedPasses = activity.waypoints().stream()
                                                   .filter(wp -> wp.latlon().isPresent())
                                                   .flatMap(wp -> crossesPassSummit(wp).stream())
                                                   .distinct()
                                                   .toList();
        return new CrossedPasses(crossedPasses);
    }

    private Optional<MountainPass> crossesPassSummit(Waypoint waypoint) {
        return mountainPasses.stream()
                             .filter(mountainPass -> mountainPass.waypointCloseToPassSummit(waypoint))
                             .findFirst();
    }
}
