package net.privactivity.store.usecase.getaggregation;

import net.privactivity.domain.Activity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EddingtonNumberCalculator {

    public int calculate(List<Activity> activities) {
        Map<LocalDate, Integer> dailyDistances = groupByDateAndSumDistancePerDay(activities);

        List<Integer> sortedDistances = new ArrayList<>(dailyDistances.values());
        sortedDistances.sort(Collections.reverseOrder());

        int eddingtonNumber = 0;

        // The Eddington number is the highest value E such that there are at least E days with distance >= E
        for (int i = 0; i < sortedDistances.size(); i++) {
            int dayDistance = sortedDistances.get(i);

            // For Eddington number E, we need at least E days with distance >= E
            int requiredDays = i + 1; // We have examined (i+1) days so far

            if (dayDistance >= requiredDays) {
                eddingtonNumber = requiredDays;
            } else {
                // Since distances are sorted in descending order, if current day distance < requiredDays,
                // all subsequent days will also be less than their respective requiredDays
                break;
            }
        }

        return eddingtonNumber;
    }

    private static Map<LocalDate, Integer> groupByDateAndSumDistancePerDay(List<Activity> activities) {
        Map<LocalDate, Integer> dailyDistances = new HashMap<>();

        for (Activity activity : activities) {
            if (activity.totals().distanceInMeters().isPresent()) {
                LocalDate date = activity.start().toLocalDate();
                int distanceInMeters = activity.totals().distanceInMeters().orThrow();

                dailyDistances.merge(date, distanceInMeters / 1000, Integer::sum);
            }
        }
        return dailyDistances;
    }
}