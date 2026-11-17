package net.privactivity.store.usecase.importactivities;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.fit.decode.FitDecoderImpl;
import net.privactivity.gpx.GpxImporterImpl;
import net.privactivity.store.model.MapBasedActivityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImportActivitiesUseCaseTest {

    @TempDir
    Path importDirectory;

    @Test
    void importDirectory_shouldImportSinglePlainFitFile() throws Exception {
        Path fitFile = importDirectory.resolve("2025-04-04-04-04-44.fit");
        copyFitToImportDir(fitFile);
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());

        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                new GpxImporterImpl(), null, _ -> List.of());

        testee.importDirectory(importDirectory, false);

        long expectedId = epochMillis(2025, 4, 4, 4, 4, 44);
        assertThat(activityRepository.getAll(false))
                .extracting(Activity::id)
                .containsExactly(expectedId);
    }

    @Test
    void importDirectory_shouldKeepZipEntryIdBehavior() throws Exception {
        long expectedId = 123456L;
        createFitZip(importDirectory.resolve("activity.zip"), expectedId + "_activity.fit");
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());
        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                new GpxImporterImpl(), null, _ -> List.of());

        List<Long> importedIds = testee.importDirectory(importDirectory, false);

        assertThat(importedIds).containsExactly(expectedId);
        assertThat(activityRepository.getActivityById(expectedId, false).id()).isEqualTo(expectedId);
    }

    @Test
    void importDirectory_shouldAssignZipEntryIdAfterDecodingGpx() throws Exception {
        long expectedId = 987654L;
        try (InputStream fixture = Objects.requireNonNull(getClass().getResourceAsStream("/net/privactivity/gpx/import.gpx"));
             ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(importDirectory.resolve("activity.zip")))) {
            zip.putNextEntry(new ZipEntry(expectedId + "_activity.gpx"));
            fixture.transferTo(zip);
            zip.closeEntry();
        }
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());
        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                new GpxImporterImpl(), null, _ -> List.of());

        assertThat(testee.importDirectory(importDirectory, false)).containsExactly(expectedId);
        Activity activity = activityRepository.getActivityById(expectedId, true);
        assertThat(activity.id()).isEqualTo(expectedId);
        assertThat(activity.title()).isEqualTo("Track 044");
    }

    @Test
    void importDirectory_shouldAssignZipEntryIdAndPreserveTcxTitleAfterDecoding() throws Exception {
        long expectedId = 123456L;
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(importDirectory.resolve("activity.zip")))) {
            zip.putNextEntry(new ZipEntry(expectedId + "_activity.tcx"));
            zip.closeEntry();
        }
        Activity decoded = new Activity(999L, "TCX from Garmin ANT Agent", ZonedDateTime.now(), List.of(), Duration.ZERO);
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());
        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                new GpxImporterImpl(), _ -> decoded, _ -> List.of());

        assertThat(testee.importDirectory(importDirectory, false)).containsExactly(expectedId);
        Activity imported = activityRepository.getActivityById(expectedId, false);
        assertThat(imported.id()).isEqualTo(expectedId);
        assertThat(imported.title()).isEqualTo("TCX 123456 from Garmin ANT Agent");
        assertThat(testee.importDirectory(importDirectory, false)).isEmpty();
    }

    @Test
    void importDirectory_shouldUseFirstWaypointTimeForPlainFitWithArbitraryFilename() throws Exception {
        copyFitToImportDir(importDirectory.resolve("morning-ride.fit"));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());
        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                new GpxImporterImpl(), null, _ -> List.of());

        List<Long> importedIds = testee.importDirectory(importDirectory, false);

        Activity activity = activityRepository.getActivityById(importedIds.getFirst(), true);
        long expectedId = activity.start()
                                  .plusSeconds(activity.waypoints().getFirst().secondsSinceStart())
                                  .toInstant()
                                  .toEpochMilli();
        assertThat(importedIds).containsExactly(expectedId);
        assertThat(activity.id()).isEqualTo(expectedId);
    }

    @Test
    void importDirectory_shouldRemoveExistingActivitiesBeforeImportWhenRequested() throws Exception {
        copyFitToImportDir(importDirectory.resolve("2026-04-04-04-04-44.fit"));
        Activity existingActivity = new Activity(1L, "Existing activity", ZonedDateTime.now(), List.of(), Duration.ZERO);
        MapBasedActivityRepository activityRepository =
                new MapBasedActivityRepository(Map.of(existingActivity.id(), existingActivity));

        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                                                                                                                          new GpxImporterImpl(), null, _ -> List.of());

        testee.importDirectory(importDirectory, true);

        long expectedId = epochMillis(2026, 4, 4, 4, 4, 44);
        assertThat(activityRepository.getAll(false))
                .extracting(Activity::id)
                .containsExactly(expectedId);
    }

    @Test
    void importDirectory_shouldReturnIdsOfAllImportedActivities() throws Exception {
        copyFitToImportDir(importDirectory.resolve("2026-04-04-04-04-44.fit"));
        copyFitToImportDir(importDirectory.resolve("2026-05-05-05-05-55.fit"));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());

        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                                                                     new GpxImporterImpl(), null, _ -> List.of());

        List<Long> importedIds = testee.importDirectory(importDirectory, false);

        long firstExpectedId = epochMillis(2026, 4, 4, 4, 4, 44);
        long secondExpectedId = epochMillis(2026, 5, 5, 5, 5, 55);
        assertThat(importedIds).containsExactlyInAnyOrder(firstExpectedId, secondExpectedId);
    }

    @Test
    void importDirectory_shouldImportSinglePlainGpxFile() throws Exception {
        copyGpxToImportDir(importDirectory.resolve("2026-04-04-04-04-44.gpx"));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());

        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                new GpxImporterImpl(), null, _ -> List.of());

        List<Long> importedIds = testee.importDirectory(importDirectory, false);

        long expectedId = epochMillis(2026, 4, 4, 4, 4, 44);
        assertThat(importedIds).containsExactly(expectedId);
        Activity activity = activityRepository.getActivityById(expectedId, true);
        assertThat(activity.title()).isEqualTo("Track 044");
        assertThat(activity.waypoints()).hasSize(491);
    }

    @Test
    void importDirectory_shouldUseFirstWaypointTimeForPlainGpxWithArbitraryFilename() throws Exception {
        copyGpxToImportDir(importDirectory.resolve("weekend-loop.gpx"));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());
        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                new GpxImporterImpl(), null, _ -> List.of());

        List<Long> importedIds = testee.importDirectory(importDirectory, false);

        Activity activity = activityRepository.getActivityById(importedIds.getFirst(), true);
        long expectedId = activity.start()
                                  .plusSeconds(activity.waypoints().getFirst().secondsSinceStart())
                                  .toInstant()
                                  .toEpochMilli();
        assertThat(importedIds).containsExactly(expectedId);
        assertThat(activity.id()).isEqualTo(expectedId);
    }


    @Test
    void importDirectory_shouldNotImportPlainGpxFileTwice() throws Exception {
        copyGpxToImportDir(importDirectory.resolve("2026-04-04-04-04-44.gpx"));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());

        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                                                                     new GpxImporterImpl(), null, _ -> List.of());

        testee.importDirectory(importDirectory, false);
        List<Long> secondImport = testee.importDirectory(importDirectory, false);

        assertThat(secondImport).isEmpty();
        assertThat(activityRepository.getAll(false)).hasSize(1);
    }

    @Test
    void importDirectory_shouldNotImportPlainFilesWithDuplicateWaypointIds() throws Exception {
        copyFitToImportDir(importDirectory.resolve("first-ride.fit"));
        copyFitToImportDir(importDirectory.resolve("second-ride.fit"));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());
        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                                                                     new GpxImporterImpl(), null, _ -> List.of());

        List<Long> importedIds = testee.importDirectory(importDirectory, false);

        assertThat(importedIds).hasSize(1);
        assertThat(activityRepository.getAll(false)).hasSize(1);
    }

    @Test
    void importDirectory_shouldAssignFirstWaypointTimeAfterDecoding() throws Exception {
        Files.createFile(importDirectory.resolve("offset.fit"));
        ZonedDateTime start = ZonedDateTime.parse("2020-01-02T03:04:05Z");
        Waypoint waypoint = new Waypoint(37, Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(),
                                         Maybe.none(), Maybe.none(), 1);
        Activity decoded = new Activity(999L, "Ride", start, List.of(waypoint), Duration.ZERO);
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());
        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, _ -> decoded,
                new GpxImporterImpl(), null, _ -> List.of());

        long expectedId = start.toInstant().plusSeconds(37).toEpochMilli();
        assertThat(testee.importDirectory(importDirectory, false)).containsExactly(expectedId);
        assertThat(activityRepository.getActivityById(expectedId, true).id()).isEqualTo(expectedId);
    }

    @Test
    void importDirectory_shouldFailWhenPlainFilenameHasNoTimestampAndActivityHasNoWaypoints() throws Exception {
        Files.createFile(importDirectory.resolve("empty.fit"));
        Activity activityWithoutWaypoints =
                new Activity(0L, "Empty activity", ZonedDateTime.now(), List.of(), Duration.ZERO);
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());
        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository,
                                                                      is -> {
                                                                          assertThat(is).isNotNull();
                                                                          return activityWithoutWaypoints;
                                                                      }, new GpxImporterImpl(), null, _ -> List.of());

        assertThatThrownBy(() -> testee.importDirectory(importDirectory, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no waypoints found");
        assertThat(activityRepository.getAll(false)).isEmpty();
    }

    private long epochMillis(int year, int month, int day, int hour, int minute, int second) {
        return LocalDateTime.of(year, month, day, hour, minute, second)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli();
    }

    private void copyFitToImportDir(Path fitFile) throws IOException {
        copyFixture("/Activity.fit", fitFile);
    }

    private void copyGpxToImportDir(Path gpxFile) throws IOException {
        copyFixture("/net/privactivity/gpx/import.gpx", gpxFile);
    }

    private void createFitZip(Path zipFile, String entryName) throws IOException {
        try (InputStream fixture = Objects.requireNonNull(getClass().getResourceAsStream("/Activity.fit"));
             ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            zip.putNextEntry(new ZipEntry(entryName));
            fixture.transferTo(zip);
            zip.closeEntry();
        }
    }

    private void copyFixture(String resource, Path target) throws IOException {
        try (InputStream fixture = Objects.requireNonNull(getClass().getResourceAsStream(resource))) {
            Files.copy(fixture, target);
        }
    }
}
