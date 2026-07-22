package net.privactivity.store.service;

import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.adapter.TilesRepository;
import net.privactivity.store.usecase.gettiles.GetTilesUseCase;
import net.privactivity.store.usecase.gettiles.TileBounds;
import net.privactivity.store.usecase.gettiles.TileMetric;
import net.privactivity.store.usecase.gettiles.TilesResponse;
import net.privactivity.store.usecase.rebuildtiles.RebuildTilesSnapshotUseCase;
import net.privactivity.store.usecase.rebuildtiles.TilesProperties;
import org.springframework.stereotype.Service;

@Service
public class TilesService {

    private final ActivityRepository activityRepository;
    private final TilesRepository tilesRepository;
    private final TilesProperties tilesProperties;

    public TilesService(ActivityRepository activityRepository,
                        TilesRepository tilesRepository,
                        TilesProperties tilesProperties) {
        this.activityRepository = activityRepository;
        this.tilesRepository = tilesRepository;
        this.tilesProperties = tilesProperties;
    }

    public TilesResponse getTiles(TileMetric metric, TileBounds bounds) {
        GetTilesUseCase useCase = new GetTilesUseCase(tilesRepository);
        return useCase.getTiles(metric, bounds);
    }

    public synchronized void rebuildSnapshot() {
        RebuildTilesSnapshotUseCase useCase = new RebuildTilesSnapshotUseCase(activityRepository,
                                                                              tilesProperties,
                                                                              tilesRepository);
        useCase.rebuildSnapshot();
    }
}