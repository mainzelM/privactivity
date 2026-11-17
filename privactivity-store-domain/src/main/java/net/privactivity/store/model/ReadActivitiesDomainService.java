package net.privactivity.store.model;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Maybe;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.adapter.MetaData;
import net.privactivity.store.adapter.MetaDataRepository;
import net.privactivity.store.usecase.townfinder.adapter.ActivityTownsRepository;
import net.privactivity.store.usecase.townfinder.model.Town;

import java.util.List;
import java.util.stream.Stream;

public class ReadActivitiesDomainService {

    private final ActivityRepository activityRepository;
    private final MetaDataRepository metaDataRepository;
    private final ActivityTownsRepository activityTownsRepository;

    public ReadActivitiesDomainService(ActivityRepository activityRepository, MetaDataRepository metaDataRepository, ActivityTownsRepository activityTownsRepository) {
        this.activityRepository = activityRepository;
        this.metaDataRepository = metaDataRepository;
        this.activityTownsRepository = activityTownsRepository;
    }

    public Stream<Activity> fetchActivities(boolean attachWaypoints) {
        List<Activity> activities = activityRepository.getAll(attachWaypoints);
        return activities.stream()
                .map(this::addMetaData);
    }

    private Activity addMetaData(Activity activity) {
        Maybe<MetaData> metaData = metaDataRepository.getMetaDataForActivity(activity.id());
        if (metaData.isPresent()) {
            MetaData md = metaData.orThrow();
            if (md.manualTitle().isPresent()) {
                return activity.withTitle(md.manualTitle().orThrow());
            } else {
                return automaticTitle(activity);
            }
        } else {
            return automaticTitle(activity);
        }
    }

    private Activity automaticTitle(Activity activity) {
        if (activity.title().contains("Rennradfahren") || activity.title().isEmpty()) {
            List<String> towns = townsForTitle(activity);
            if (towns.isEmpty()) {
                return activity;
            } else {
                return activity.withTitle(String.join(", ", towns));
            }
        } else {
            return activity;
        }
    }

    private List<String> townsForTitle(Activity activity) {
        Maybe<List<Town>> storedTowns = activityTownsRepository.townsOfActivity(activity);
        if (storedTowns.isPresent()) {
            return storedTowns.orThrow().stream()
                    .map(Town::name)
                    .toList();
        } else {
            return List.of();
        }
    }

    public Activity getActivityById(long trackId, boolean includeWaypoints) {
        Activity activity = activityRepository.getActivityById(trackId, includeWaypoints);
        return addMetaData(activity);
    }
}
