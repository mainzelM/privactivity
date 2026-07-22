package net.privactivity.store.service;

import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.usecase.export.adapter.GpxExporter;
import net.privactivity.store.usecase.export.exportascsv.CsvExportConfig;
import net.privactivity.store.usecase.export.exportascsv.CsvExportResult;
import net.privactivity.store.usecase.export.exportascsv.ExportAsCsvUseCase;
import net.privactivity.store.usecase.export.exportasgpx.ExportAsGpxUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExportService {
    private final ActivityRepository activityRepository;
    private final GpxExporter gpxExporter;

    @Autowired
    public ExportService(ActivityRepository activityRepository, GpxExporter gpxExporter) {
        this.activityRepository = activityRepository;
        this.gpxExporter = gpxExporter;
    }

    public String exportActivityAsGpx(long activityId, float speedFactor, int startDiffHours) {
        ExportAsGpxUseCase useCase = new ExportAsGpxUseCase(activityRepository, gpxExporter);
        return useCase.export(activityId, speedFactor, startDiffHours);
    }

    public CsvExportResult exportActivityAsCsv(long trackId, CsvExportConfig csvExportConfig) {
        ExportAsCsvUseCase useCase = new ExportAsCsvUseCase(activityRepository);
        return useCase.exportActivityAsCsv(trackId, csvExportConfig);
    }
}
