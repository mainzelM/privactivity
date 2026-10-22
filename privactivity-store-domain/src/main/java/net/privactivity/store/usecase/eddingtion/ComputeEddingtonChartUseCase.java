package net.privactivity.store.usecase.eddingtion;

import net.privactivity.domain.Activity;
import net.privactivity.store.adapter.ActivityRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeEddingtonChartUseCase {

    private final ActivityRepository activityRepository;

    public ComputeEddingtonChartUseCase(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public record EddingtonChart(List<Integer> countsPerKM, int eddigtionNumber, int missingToNext) {
    }

    public EddingtonChart act() {

        List<Integer> sortedDistances =
                new ArrayList<>(groupByDateAndSumDistancePerDay(activityRepository.getAll(false)));
        sortedDistances.sort(Collections.reverseOrder());

        if (sortedDistances.isEmpty()) {
            return new EddingtonChart(Collections.emptyList(), 0, 0);
        }
        int highest = sortedDistances.getFirst();
        List<Integer> numActivitiesPerKm = new ArrayList<>(Arrays.asList(new Integer[highest + 1]));
        int currentCount = 0;
        int currentActivityIndex = 0;
        int eddingtonNumber = 0;
        for (int i = highest; i >= 0; i--) {
            while (currentActivityIndex < sortedDistances.size()) {
                int activityKm = sortedDistances.get(currentActivityIndex);
                if (activityKm >= i) {
                    currentCount++;
                    currentActivityIndex++;
                } else {
                    break;
                }
            }
            numActivitiesPerKm.set(i, currentCount);
            if (currentCount >= i) {
                eddingtonNumber = Math.max(eddingtonNumber, i);
            }
        }
        int nextEddingtonNumber = eddingtonNumber + 1;
        int missingToNext = missingToNext(numActivitiesPerKm, nextEddingtonNumber);
        return new EddingtonChart(numActivitiesPerKm, eddingtonNumber, missingToNext);
    }

    private int missingToNext(List<Integer> numActivitiesPerKm, int nextEddingtonNumber) {
        if (numActivitiesPerKm.size() > nextEddingtonNumber) {
            return nextEddingtonNumber - numActivitiesPerKm.get(nextEddingtonNumber);
        } else {
            return 0;
        }
    }

    private Collection<Integer> groupByDateAndSumDistancePerDay(List<Activity> activities) {
        Map<LocalDate, Integer> dailyDistances = new HashMap<>();

        for (Activity activity : activities) {
            LocalDate date = activity.start().toLocalDate();
            int distanceInMeters = activity.totals().distanceInMeters().orElse(0);

            dailyDistances.merge(date, distanceInMeters / 1000, Integer::sum);
        }
        return dailyDistances.values();
    }
}
