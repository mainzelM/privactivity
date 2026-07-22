package net.privactivity.store.admin;

import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.config.PrivactivityUserDetailsManager;
import net.privactivity.store.service.ImportActivitiesService;
import net.privactivity.store.service.TilesService;
import net.privactivity.store.service.TownFinderService;
import net.privactivity.store.usecase.gettiles.TileMetric;
import net.privactivity.store.usecase.gettiles.TilesResponse;
import net.privactivity.store.usecase.townfinder.addtowns.model.AddedAndExistingTowns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminResource {

    private static final Logger logger = LoggerFactory.getLogger(AdminResource.class);

    private final ActivityRepository activityRepository;
    private final Environment environment;
    private final Optional<BuildProperties> buildProperties;
    private final TownFinderService townFinderService;
    private final ImportActivitiesService importActivitiesService;
    private final TilesService tilesService;
    private final String fitImportPath;
    private final PrivactivityUserDetailsManager userDetailsManager;

    @Autowired
    public AdminResource(ActivityRepository activityRepository,
                         Environment environment,
                         Optional<BuildProperties> buildProperties, TownFinderService townFinderService,
                         ImportActivitiesService importActivitiesService,
                         TilesService tilesService,
                         @Value("${privactivity.fit.import.directory}") String fitImportPath,
                         PrivactivityUserDetailsManager userDetailsManager) {
        this.importActivitiesService = importActivitiesService;
        this.tilesService = tilesService;
        this.fitImportPath = fitImportPath;

        this.activityRepository = activityRepository;
        this.environment = environment;
        this.buildProperties = buildProperties;
        this.townFinderService = townFinderService;
        this.userDetailsManager = userDetailsManager;
    }

    @GetMapping("/system-info")
    public SystemInfoResponse getSystemInfo() {
        SystemInfoResponse response = new SystemInfoResponse();

        // Application info
        response.applicationName = buildProperties.map(BuildProperties::getName).orElse("Privactivity Activity Store");
        response.version = buildProperties.map(BuildProperties::getVersion).orElse("development");
        response.buildTime = buildProperties.map(bp -> Objects.requireNonNull(bp.getTime()).toString()).orElse(
                "unknown");
        response.javaVersion = buildProperties.map(bp -> bp.get("java.version")).orElse("unknown");
        response.gradleVersion = buildProperties.map(bp -> bp.get("gradle.version")).orElse("unknown");
        response.gitCommit = buildProperties.map(bp -> bp.get("git.commit")).orElse("unknown");
        response.buildNumber = buildProperties.map(bp -> bp.get("build.number")).orElse("unknown");
        response.activeProfiles = environment.getActiveProfiles();

        // Storage statistics
        StorageStats storageStats = new StorageStats();
        storageStats.fitActivities = activityRepository.getAll(false).size();
        storageStats.gpxActivities = 0; // Placeholder until GPX store is implemented
        storageStats.totalActivities = storageStats.fitActivities + storageStats.gpxActivities;
        storageStats.storageSize = "N/A"; // Could implement actual size calculation
        response.storageStats = storageStats;

        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        MemoryUsage memoryUsage = new MemoryUsage();
        memoryUsage.used = formatBytes(runtime.totalMemory() - runtime.freeMemory());
        memoryUsage.total = formatBytes(runtime.totalMemory());
        memoryUsage.max = formatBytes(runtime.maxMemory());
        response.memoryUsage = memoryUsage;

        return response;
    }

    @PostMapping("/import-all")
    public ImportResult importAllActivities(
            @RequestParam(value = "removeAllBeforeImport", defaultValue = "false") boolean removeAllBeforeImport) {
        ImportResult result = new ImportResult();

        try {
            logger.info("Importing all activitiies from {}", fitImportPath);
            importActivitiesService.importDirectory(Path.of(fitImportPath), removeAllBeforeImport);
            logger.info("Import finished");
            int imported = 0;
            int skipped = 0;
            int errors = 0;

            result.message = "Import operation completed (placeholder implementation)";
            result.imported = imported;
            result.skipped = skipped;
            result.errors = errors;

        } catch (Exception e) {
            logger.error("Error while importing activitiies from {}", fitImportPath, e);
            result.message = "Import failed: " + e.getMessage();
            result.imported = 0;
            result.skipped = 0;
            result.errors = 1;
        }

        return result;
    }

    @PostMapping("/clear-cache")
    public CacheResult clearCache() {
        CacheResult result = new CacheResult();

        try {
            // Trigger garbage collection
            activityRepository.gc();
            System.gc();

            result.message = "Cache cleared and garbage collection triggered";
            result.clearedEntries = 0; // Placeholder

        } catch (Exception e) {
            result.message = "Cache clearing failed: " + e.getMessage();
            result.clearedEntries = 0;
        }

        return result;
    }

    @PostMapping("/rebuild-tiles")
    public TilesResponse rebuildTiles() {
        tilesService.rebuildSnapshot();
        return tilesService.getTiles(TileMetric.ACTIVITY_COUNT, null);
    }

    @PostMapping("/addMissingTowns")
    public AddMissingTownsDTO addMissingTowns() {
        AddedAndExistingTowns addedAndExistingTowns = townFinderService.addMissingTowns();
        return new AddMissingTownsDTO(addedAndExistingTowns);
    }

    @PostMapping("/change-password")
    public ChangePasswordResult changePassword(@RequestBody ChangePasswordRequest request,
                                               Authentication authentication) {
        try {
            String username = authentication.getName();
            boolean success = userDetailsManager.changeUserPassword(username, request.currentPassword(),
                                                                    request.newPassword());

            if (success) {
                logger.info("User {} successfully changed password", username);
                return new ChangePasswordResult(true, "Password changed successfully");
            } else {
                logger.warn("User {} provided incorrect password while trying to change password", username);
                return new ChangePasswordResult(false, "Current password is incorrect");
            }
        } catch (Exception e) {
            logger.error("Failed to change password for user: {}", authentication.getName(), e);
            return new ChangePasswordResult(false, "Failed to change password: " + e.getMessage());
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    // Response DTOs
    public static class SystemInfoResponse {
        public String applicationName;
        public String version;
        public String buildTime;
        public String javaVersion;
        public String gradleVersion;
        public String gitCommit;
        public String buildNumber;
        public String[] activeProfiles;
        public StorageStats storageStats;
        public MemoryUsage memoryUsage;
    }

    public static class StorageStats {
        public int fitActivities;
        public int gpxActivities;
        public int totalActivities;
        public String storageSize;
    }

    public static class MemoryUsage {
        public String used;
        public String total;
        public String max;
    }

    public static class ImportResult {
        public String message;
        public int imported;
        public int skipped;
        public int errors;
    }

    public static class CacheResult {
        public String message;
        public int clearedEntries;
    }

    public static class AddMissingTownsDTO {
        public int added;
        public int existing;

        public AddMissingTownsDTO(AddedAndExistingTowns addedAndExistingTowns) {
            this.added = addedAndExistingTowns.added();
            this.existing = addedAndExistingTowns.existing();
        }
    }
}