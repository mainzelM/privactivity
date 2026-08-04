package net.privactivity.store.usecase.getaggregation;

import net.privactivity.domain.Activity;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.usecase.getaggregation.model.ActivityAggregatorDomainService;
import net.privactivity.store.usecase.getaggregation.model.AggregationFilter;
import net.privactivity.store.usecase.getaggregation.model.TotalAggregation;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class AggregatorUseCase {

    private final ActivityAggregatorDomainService aggregator;
    private final ActivityRepository activityRepository;
    private final EddingtonNumberCalculator eddingtonNumberCalculator;

    public AggregatorUseCase(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
        aggregator = new ActivityAggregatorDomainService();
        eddingtonNumberCalculator = new EddingtonNumberCalculator();
    }

    public TotalAggregation aggregate() {
        List<Activity> activities = activityRepository.getAll(false);
        return aggregator.aggregate(activities);
    }

    public TotalAggregation aggregateForMonths(int[] monthArray) {
        List<Activity> activities = activityRepository.getAll(false);
        TotalAggregation aggregate = aggregator.aggregate(activities);
        AggregationFilter filter = new AggregationFilter();
        return filter.filterByMonths(aggregate, monthArray);
    }

    public TotalAggregation aggregateYTD() {
        List<Activity> activities = activityRepository.getAll(false);
        ZonedDateTime now = ZonedDateTime.now();
        Predicate<Activity> upToNow = a -> a.start().getDayOfYear() <= now.getDayOfYear();
        int[] months = IntStream.rangeClosed(1, now.getMonthValue()).toArray();
        TotalAggregation aggregate = aggregator.aggregateFiltered(activities, upToNow);
        AggregationFilter filter = new AggregationFilter();
        return filter.filterByMonths(aggregate, months);
    }

    public int eddingtonNumber() {
        List<Activity> activities = activityRepository.getAll(false);
        return eddingtonNumberCalculator.calculate(activities);
    }
}
