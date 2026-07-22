package net.privactivity.store.unified;

import net.privactivity.domain.Activity;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.adapter.TilesRepository;
import net.privactivity.store.model.Filter;
import net.privactivity.store.service.ExportService;
import net.privactivity.store.service.PrivactivityStoreService;
import net.privactivity.store.usecase.export.exportascsv.CsvExportConfig;
import net.privactivity.store.usecase.export.exportascsv.CsvExportResult;
import net.privactivity.store.usecase.findActivities.AndFilter;
import net.privactivity.store.usecase.findActivities.AscentFilter;
import net.privactivity.store.usecase.findActivities.DistanceFilter;
import net.privactivity.store.usecase.findActivities.EmptyFilter;
import net.privactivity.store.usecase.findActivities.LocationFilter;
import net.privactivity.store.usecase.findActivities.TileFilter;
import net.privactivity.store.usecase.findActivities.TitleFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/activities")
public class PrivactivityResource {

    private final ActivityRepository activityRepository;
    private final PrivactivityStoreService privactivityStoreService;
    private final TilesRepository tilesRepository;
    private final ExportService exportService;

    @Autowired
    public PrivactivityResource(ActivityRepository activityRepository,
                                PrivactivityStoreService privactivityStoreService, TilesRepository tilesRepository,
                                ExportService exportService) {
        this.activityRepository = activityRepository;
        this.privactivityStoreService = privactivityStoreService;
        this.tilesRepository = tilesRepository;
        this.exportService = exportService;
    }

    @GetMapping("")
    public List<ActivityDTO> findActivities(ActivityFilterRequest request) {

        Filter filter = mapFilter(request);

        boolean includeWaypoints = Boolean.TRUE.equals(request.includeWaypoints());
        String sortBy = request.sortBy() != null ? request.sortBy() : "date";
        List<Activity> activities = privactivityStoreService.getAllActivities(
                includeWaypoints, filter, sortBy, request.sortDirection());

        return activities.stream()
                         .map(ActivityDTO::new)
                         .collect(Collectors.toList());
    }

    private Filter mapFilter(ActivityFilterRequest request) {
        List<Filter> filters = new ArrayList<>();

        if (request.minDistance() != null || request.maxDistance() != null) {
            int min = request.minDistance() != null ? request.minDistance() : 0;
            int max = request.maxDistance() != null ? request.maxDistance() : Integer.MAX_VALUE;
            filters.add(new DistanceFilter(min, max));
        }

        if (request.minAscent() != null || request.maxAscent() != null) {
            int min = request.minAscent() != null ? request.minAscent() : 0;
            int max = request.maxAscent() != null ? request.maxAscent() : Integer.MAX_VALUE;
            filters.add(new AscentFilter(min, max));
        }

        if (request.lat() != null && request.lon() != null) {
            int radius = request.radius() != null ? request.radius() : 5000;
            filters.add(new LocationFilter(request.lat(), request.lon(), radius));
        }

        if (request.title() != null && !request.title().isBlank()) {
            filters.add(new TitleFilter(request.title()));
        }

        if (request.tileId() != null) {
            filters.add(new TileFilter(tilesRepository.getTileById(request.tileId())));
        }

        if (filters.isEmpty()) {
            return new EmptyFilter();
        } else if (filters.size() == 1) {
            return filters.getFirst();
        } else {
            return new AndFilter(filters);
        }
    }

    @GetMapping("/{trackId}")
    public ActivityDTO activity(@PathVariable("trackId") String trackId) {
        Activity activity = privactivityStoreService.getActivity(Long.parseLong(trackId), true);
        return new ActivityDTO(activity);
    }

    @PutMapping("/{trackId}/title")
    public void changeTitle(@PathVariable long trackId, @RequestBody ChangeTitleRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Activity title must not be blank.");
        }
        privactivityStoreService.changeTitle(trackId, request.title().trim());
    }

    @GetMapping(value = "/{activityId}/export", produces = "application/xml")
    public String exportAsGpx(@PathVariable long activityId,
                              @RequestParam(value = "speedFactor", defaultValue = "1") float speedFactor,
                              @RequestParam(value = "startDiffHours", defaultValue = "1") int startDiffHours) {

        return exportService.exportActivityAsGpx(activityId, speedFactor, startDiffHours);
    }

    @PostMapping(value = "/{trackId}/export.csv", produces = "text/csv", consumes = "application/json")
    public ResponseEntity<String> exportAsCsv(@PathVariable long trackId,
                                              @RequestBody(required = false) CsvExportConfigRequest request) {
        CsvExportConfigRequest effectiveRequest = request != null ? request : CsvExportConfigRequest.defaults();
        CsvExportConfig csvExportConfig = effectiveRequest.toCsvExporterConfig();
        CsvExportResult csvExportResult = exportService.exportActivityAsCsv(trackId, csvExportConfig);
        return ResponseEntity.ok()
                             .header(HttpHeaders.CONTENT_DISPOSITION,
                                     "attachment; filename=\"" + csvExportResult.fileName() + "\"")
                             .contentType(MediaType.parseMediaType("text/csv"))
                             .body(csvExportResult.csvContent());
    }

    @DeleteMapping("")
    public void removeAll() {
        activityRepository.removeAll();
    }
}
