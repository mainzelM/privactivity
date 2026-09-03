package net.privactivity.store.service;

import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.adapter.MountainPassRepository;
import net.privactivity.store.usecase.allcrossedpasses.AllCrossedPasses;
import net.privactivity.store.usecase.allcrossedpasses.GetAllCrossedPassesUseCase;
import net.privactivity.store.usecase.crossedpasses.CrossedPasses;
import net.privactivity.store.usecase.crossedpasses.GetCrossedPassesUseCase;
import org.springframework.stereotype.Service;

@Service
public class MountainPassService {

    private final MountainPassRepository mountainPassRepository;
    private final ActivityRepository activityRepository;

    public MountainPassService(MountainPassRepository mountainPassRepository, ActivityRepository activityRepository) {
        this.mountainPassRepository = mountainPassRepository;
        this.activityRepository = activityRepository;
    }

    public CrossedPasses crossedPasses(long activityId) {
        GetCrossedPassesUseCase getCrossedPassesUseCase = new GetCrossedPassesUseCase(activityRepository,
                                                                                      mountainPassRepository);
        return getCrossedPassesUseCase.act(activityId);
    }

    public AllCrossedPasses allCrossedPasses() {
        GetAllCrossedPassesUseCase getAllCrossedPassesUseCase = new GetAllCrossedPassesUseCase(activityRepository,
                                                                                               mountainPassRepository);
        return getAllCrossedPassesUseCase.act();
    }
}
