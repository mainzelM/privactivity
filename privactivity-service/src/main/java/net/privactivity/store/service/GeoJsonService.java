package net.privactivity.store.service;

import net.privactivity.domain.ClimbPointer;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.geojson.usecase.getgeojson.GetGeoJsonUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class GeoJsonService {

    private final ActivityRepository activityRepository;

    @Autowired
    public GeoJsonService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public Object asGeoJson(long activityId, boolean includeClimbs) {
        GetGeoJsonUseCase useCase = new GetGeoJsonUseCase(activityRepository);
        List<ClimbPointer> climbs = includeClimbs ? /*climbDetectorConnector.detectClimbsOf(a)*/ null :
                                    Collections.emptyList();
        return useCase.asGeoJson(activityId, climbs);
    }
}
