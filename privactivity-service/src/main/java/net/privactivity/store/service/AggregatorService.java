package net.privactivity.store.service;

import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.usecase.eddingtion.ComputeEddingtonChartUseCase;
import net.privactivity.store.usecase.getaggregation.AggregatorUseCase;
import net.privactivity.store.usecase.getaggregation.model.TotalAggregation;
import net.privactivity.store.usecase.movingsum.ComputeMovingSumUseCase;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Map;

@Service
public class AggregatorService {
    private final ActivityRepository activityRepository;

    public AggregatorService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public TotalAggregation getTotalAggregation() {
        AggregatorUseCase aggregatorUseCase = new AggregatorUseCase(activityRepository);
        return aggregatorUseCase.aggregate();
    }


    public TotalAggregation aggregateForMonths(int[] monthArray) {
        AggregatorUseCase aggregatorUseCase = new AggregatorUseCase(activityRepository);
        return aggregatorUseCase.aggregateForMonths(monthArray);
    }

    public TotalAggregation aggregateYTD() {
        AggregatorUseCase aggregatorUseCase = new AggregatorUseCase(activityRepository);
        return aggregatorUseCase.aggregateYTD();
    }

    public Map<LocalDate, Integer> movingSum(int numDays) {
        ComputeMovingSumUseCase computeMovingSumUseCase = new ComputeMovingSumUseCase(activityRepository,
                                                                                      LocalDate.now());
        return computeMovingSumUseCase.compute(numDays);
    }

    public ComputeEddingtonChartUseCase.EddigtionChart eddingtionChart() {
        ComputeEddingtonChartUseCase testee = new ComputeEddingtonChartUseCase(activityRepository);
        return testee.act();
    }
}
