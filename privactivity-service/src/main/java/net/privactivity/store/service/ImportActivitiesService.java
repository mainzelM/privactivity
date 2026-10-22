package net.privactivity.store.service;

import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.usecase.importactivities.ImportActivitiesUseCase;
import net.privactivity.store.usecase.importactivities.adapter.ActivitiesJsonReader;
import net.privactivity.store.usecase.importactivities.adapter.FitDecoder;
import net.privactivity.store.usecase.importactivities.adapter.GpxImporter;
import net.privactivity.store.usecase.importactivities.adapter.TcxImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.util.List;

@Service
public class ImportActivitiesService {

    private final ActivityRepository activityRepository;
    private final FitDecoder fitDecoder;
    private final GpxImporter gpxImporter;
    private final TcxImporter tcxImporter;
    private final ActivitiesJsonReader activitiesJsonReader;
    private final TownFinderService townFinderService;
    private final TilesService tilesService;

    @Autowired
    ImportActivitiesService(ActivityRepository activityRepository, FitDecoder fitDecoder, GpxImporter gpxImporter,
                            TcxImporter tcxImporter, ActivitiesJsonReader activitiesJsonReader,
                            TownFinderService townFinderService, TilesService tilesService) {
        this.activityRepository = activityRepository;
        this.fitDecoder = fitDecoder;
        this.gpxImporter = gpxImporter;
        this.tcxImporter = tcxImporter;
        this.activitiesJsonReader = activitiesJsonReader;
        this.townFinderService = townFinderService;
        this.tilesService = tilesService;
    }

    public List<Long> importDirectory(Path importDir, boolean removeAllBeforeImport) {
        List<Long> importedIds = importActivities(importDir, removeAllBeforeImport);
        if (!importedIds.isEmpty()) {
            townFinderService.addMissingTowns();
            tilesService.rebuildSnapshot();
        }

        return importedIds;
    }

    private List<Long> importActivities(Path importDir, boolean removeAllBeforeImport) {
        ImportActivitiesUseCase useCase = new ImportActivitiesUseCase(activityRepository, fitDecoder,
                                                                      gpxImporter, tcxImporter,
                                                                      activitiesJsonReader);
        return useCase.importDirectory(importDir, removeAllBeforeImport);
    }
}
