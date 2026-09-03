package net.privactivity.store.usecase.allcrossedpasses;

import net.privactivity.domain.Activity;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.adapter.MountainPassRepository;
import net.privactivity.store.usecase.crossedpasses.MountainPass;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GetAllCrossedPassesUseCase {
    private final ActivityRepository activityRepository;
    private final List<MountainPass> mountainPasses;

    public GetAllCrossedPassesUseCase(ActivityRepository activityRepository,
                                      MountainPassRepository mountainPassRepository) {
        this.activityRepository = activityRepository;
        this.mountainPasses = mountainPassRepository.getAllPasses();
    }

    public AllCrossedPasses act() {
        List<Activity> activities = activityRepository.getAll(false);
        List<ActivityCrossingPass> singlePassCrossings = activities.parallelStream()
                                                                   .map(this::crossedPassesOf)
                                                                   .flatMap(Collection::stream)
                                                                   .toList();

        AllCrossedPasses allCrossedPasses = new AllCrossedPasses();
        for (ActivityCrossingPass crossing : singlePassCrossings) {
            allCrossedPasses = allCrossedPasses.noteCrossing(crossing.activity, crossing.mountainPass());
        }
        return allCrossedPasses;
    }

    private List<ActivityCrossingPass> crossedPassesOf(Activity activity) {
        List<MountainPass> candidatePasses = candidatePassesFor(activity);
        if (candidatePasses.isEmpty()) {
            return List.of();
        } else {
            List<ActivityCrossingPass> crossedPasses = new ArrayList<>();
            Activity activityWithWaypoints = activityRepository.getActivityById(activity.id(), true);
            for (MountainPass mountainPass : candidatePasses) {
                if (activityWithWaypoints.waypoints().stream()
                                         .filter(waypoint -> waypoint.latlon().isPresent())
                                         .anyMatch(mountainPass::waypointCloseToPassSummit)) {
                    crossedPasses.add(new ActivityCrossingPass(activity, mountainPass));
                }
            }
            return crossedPasses;
        }
    }

    private record ActivityCrossingPass(Activity activity, MountainPass mountainPass) {

    }

    private List<MountainPass> candidatePassesFor(Activity activity) {
        return mountainPasses.stream()
                             .filter(mountainPass -> activity.bounds().contains(mountainPass.summitLatLon()))
                             .toList();
    }
}
