package net.privactivity.store.service;

import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.usecase.getaggregation.AggregatorUseCase;
import net.privactivity.store.usecase.getaggregation.model.TotalAggregation;
import org.springframework.stereotype.Service;

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

    public int eddingtonNumber() {
        AggregatorUseCase aggregatorUseCase = new AggregatorUseCase(activityRepository);
        return aggregatorUseCase.eddingtonNumber();
    }

    public TotalAggregation aggregateYTD() {
        AggregatorUseCase aggregatorUseCase = new AggregatorUseCase(activityRepository);
        return aggregatorUseCase.aggregateYTD();
    }
}
