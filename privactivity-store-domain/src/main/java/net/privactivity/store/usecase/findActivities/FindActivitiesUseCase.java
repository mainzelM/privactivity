package net.privactivity.store.usecase.findActivities;

import net.privactivity.domain.Activity;
import net.privactivity.store.model.*;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FindActivitiesUseCase {

    private final ReadActivitiesDomainService readActivitiesDomainService;

    public FindActivitiesUseCase(ReadActivitiesDomainService readActivitiesDomainService) {
        this.readActivitiesDomainService = readActivitiesDomainService;
    }



    public List<Activity> findFilteredAndSortedActivities(boolean attachWaypoints, Filter filter, String sortBy,
                                                          SortDirection sortDirection) {
        Stream<Activity> filteredActivities = applyFilter(attachWaypoints, filter);

        return applySorting(sortBy, sortDirection, filteredActivities).toList();
    }

    private Stream<Activity> applyFilter(boolean attachWaypoints, Filter filter) {
        Stream<Activity> all = readActivitiesDomainService.fetchActivities(attachWaypoints);
        LocationFilter locFilter = extractLocationFilter(filter);
        if (locFilter == null) {
            return all.filter(filter::apply);

        } else {
            Filter otherFilters = removeLocationFilter(filter);
            return all.filter(otherFilters::apply)
                      .filter(locFilter::preCheck)
                      .map(a -> readActivitiesDomainService.getActivityById(a.id(), true))
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

    public Activity getActivityById(long activityId, boolean includeWaypoints) {
        return readActivitiesDomainService.getActivityById(activityId, includeWaypoints);
    }
}
