package net.privactivity.store.service;

import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.usecase.maxpower.MaxPower;
import net.privactivity.store.usecase.maxpower.MaxPowerUseCase;
import org.springframework.stereotype.Service;

@Service
public class MaxPowerService {

    private final ActivityRepository activityRepository;

    public MaxPowerService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public MaxPower maxPowerAll() {
        MaxPowerUseCase maxPowerUseCase = new MaxPowerUseCase(activityRepository, 1, 5, 10,
                                                              15, 20);
        return maxPowerUseCase.maxPowerAll();
    }

    public MaxPower maxPowerOne(long activityId) {
        MaxPowerUseCase maxPowerUseCase = new MaxPowerUseCase(activityRepository, 1, 5, 10,
                                                              15, 20);
        return maxPowerUseCase.maxPowerOne(activityId);
    }

}
