package net.privactivity.store.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.util.List;

@Component
public class FitImportScheduler {

    private static final Logger logger = LoggerFactory.getLogger(FitImportScheduler.class);
    private final ImportActivitiesService importActivitiesService;
    private final String fitImportPath;

    public FitImportScheduler(ImportActivitiesService importActivitiesService,
                              @Value("${privactivity.fit.import.directory}") String fitImportPath) {
        this.importActivitiesService = importActivitiesService;
        this.fitImportPath = fitImportPath;
    }

    @Scheduled(fixedRate = 3600000, initialDelayString = "${privactivity.fit.import.scheduler.initiaDelay}")
    public void scheduledImportAll() {
        logger.info("Scheduled import of all activities from {}", fitImportPath);
        List<Long> importedIds = importActivitiesService.importDirectory(Path.of(fitImportPath), false);
        logger.info("Scheduled import finished, added {} activities", importedIds.size());
    }
}