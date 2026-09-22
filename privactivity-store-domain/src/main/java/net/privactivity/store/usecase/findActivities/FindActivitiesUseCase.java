package net.privactivity.store.usecase.findActivities;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Maybe;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.adapter.MetaData;
import net.privactivity.store.adapter.MetaDataRepository;
import net.privactivity.store.model.Filter;
import net.privactivity.store.usecase.townfinder.adapter.ActivityTownsRepository;
import net.privactivity.store.usecase.townfinder.model.Town;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FindActivitiesUseCase {
    private final ActivityRepository activityRepository;
    private final MetaDataRepository metaDataRepository;
    private final ActivityTownsRepository activityTownsRepository;

    public FindActivitiesUseCase(ActivityRepository activityRepository,
                                 MetaDataRepository metaDataRepository,
                                 ActivityTownsRepository activityTownsRepository) {
        this.activityRepository = activityRepository;
        this.metaDataRepository = metaDataRepository;
        this.activityTownsRepository = activityTownsRepository;
    }

    private Stream<Activity> fetchActivities(boolean attachWaypoints) {
        List<Activity> activities = activityRepository.getAll(attachWaypoints);
        return activities.stream()
                         .map(this::addMetaData);
    }

    public List<Activity> findFilteredAndSortedActivities(boolean attachWaypoints, Filter filter, String sortBy,
                                                          SortDirection sortDirection) {
        Stream<Activity> filteredActivities = applyFilter(attachWaypoints, filter);

        return applySorting(sortBy, sortDirection, filteredActivities).toList();
    }

    private Stream<Activity> applyFilter(boolean attachWaypoints, Filter filter) {
        Stream<Activity> all = fetchActivities(attachWaypoints);
        LocationFilter locFilter = extractLocationFilter(filter);
        if (locFilter == null) {
            return all.filter(filter::apply);

        } else {
            Filter otherFilters = removeLocationFilter(filter);
            return all.filter(otherFilters::apply)
                      .filter(locFilter::preCheck)
                      .map(a -> getActivityById(a.id(), true))
                      .filter(locFilter::apply);
        }
    }

    private Stream<Activity> applySorting(String sortBy, SortDirection sortDirection, Stream<Activity> activities) {
        if (sortBy == null) {
            return activities;
        } else {
            Comparator<Activity> comparator = createComparator(sortBy, sortDirection);
            return activities.sorted(comparator);
        }
    }

    private Comparator<Activity> createComparator(String sortBy, SortDirection sortDirection) {
        Comparator<Activity> comparator = switch (sortBy) {
            case "id" -> Comparator.comparing(Activity::id);
            case "title" -> Comparator.comparing(Activity::title, String.CASE_INSENSITIVE_ORDER);
            case "distance" -> Comparator.comparing(
                    a -> a.totals().distanceInMeters().isPresent() ? a.totals().distanceInMeters().orThrow() : 0);
            case "ascent" -> Comparator.comparing(
                    a -> a.totals().ascent().isPresent() ? a.totals().ascent().orThrow() : 0);
            case "duration" -> Comparator.comparing(
                    a -> a.totals().movingTime().isPresent() ? a.totals().movingTime().orThrow() : Duration.ZERO);
            default -> Comparator.comparing(Activity::start);
        };

        return sortDirection == SortDirection.asc ? comparator : comparator.reversed();
    }

    private LocationFilter extractLocationFilter(Filter filter) {
        if (filter instanceof LocationFilter lf) {
            return lf;
        } else if (filter instanceof AndFilter(List<Filter> filters)) {
            return filters.stream()
                          .filter(LocationFilter.class::isInstance)
                          .map(LocationFilter.class::cast)
                          .findFirst()
                          .orElse(null);
        } else {
            return null;
        }
    }

    private Filter removeLocationFilter(Filter filter) {
        if (filter instanceof LocationFilter) {
            return new EmptyFilter();
        } else if (filter instanceof AndFilter(List<Filter> filters)) {
            List<Filter> others = filters.stream()
                                         .filter(f -> !(f instanceof LocationFilter))
                                         .toList();
            return new AndFilter(others);
        } else {
            return filter;
        }
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
