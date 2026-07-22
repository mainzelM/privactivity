package net.privactivity.store.usecase.getaggregation.model;

import net.privactivity.domain.Activity;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;



import static java.util.stream.Collectors.toList;

public class ActivityAggregatorDomainService {

    public TotalAggregation aggregateFiltered(Collection<Activity> activitites, Predicate<Activity> filter) {
        Map<Year, Map<Month, MonthlyAggregation>> x =
                activitites.stream()
                           .filter(filter)
                           .collect(Collectors.groupingBy(a -> new Year(a.start().getYear()),
                                                          Collectors.groupingBy(a -> new Month(a.start().getMonth().getValue()),
                                                                                Collectors.collectingAndThen(toList()
                                                                                        , this::sumYear))));

        NavigableMap<Year, YearlyAggregation> byYear = new TreeMap<>();
        x.forEach((year, montly) -> {
            byYear.put(year, new YearlyAggregation(new TreeMap<>(montly)));
        });
        return new TotalAggregation(byYear);
    }

    public TotalAggregation aggregate(Collection<Activity> activitites) {
        return aggregateFiltered(activitites, _ -> true);
    }

    private MonthlyAggregation sumYear(List<Activity> activitites) {
        int sumInMeters = activitites.stream()
                                     .map(Activity::totals)
                                     .map(Activity.Totals::distanceInMeters)
                                     .mapToInt(m -> m.orElse(0))
                                     .sum();

        Duration sumInSeconds = activitites.stream()
                                           .map(Activity::totals)
                                           .map(Activity.Totals::movingTime)
                                           .map(m -> m.orElse(Duration.ZERO))
                                           .reduce(Duration.ZERO, Duration::plus);
        return new MonthlyAggregation(sumInMeters / 1000, sumInSeconds);
    }
}
