package net.privactivity.store.usecase.movingsum;

import net.privactivity.domain.Activity;
import net.privactivity.store.adapter.ActivityRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;

public class ComputeMovingSumUseCase {
    private final ActivityRepository activityRepository;
    private final LocalDate now;

    public ComputeMovingSumUseCase(ActivityRepository activityRepository, LocalDate now) {
        this.activityRepository = activityRepository;
        this.now = now;
    }

    public Map<LocalDate, Integer> compute(int numDays) {
        List<Activity> activities = activityRepository.getAll(false);
        if (activities.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Activity> sorted = activities.stream()
                                          .sorted(Comparator.comparing(Activity::start))
                                          .toList();
        Activity first = sorted.getFirst();
        LocalDate firstDate = first.start().toLocalDate();
        return firstDate.datesUntil(now.plusDays(1))
                        .gather(Gatherers.windowSliding(numDays))
                        .collect(Collectors.toMap(
                                List::getLast,
                                localDates -> {
                                    LocalDate firstOfWindow = localDates.getFirst().minusDays(1);
                                    LocalDate lastOfWindow = localDates.getLast().plusDays(1);
                                    return sorted.stream()
                                                 .filter(activity -> activity.start().toLocalDate().isAfter(firstOfWindow))
                                                 .filter(activity -> activity.start().toLocalDate().isBefore(lastOfWindow))
                                                 .map(Activity::totals)
                                                 .map(Activity.Totals::distanceInMeters)
                                                 .mapToInt(m -> m.orElse(0))
                                                 .sum();
                                }));
    }
}
