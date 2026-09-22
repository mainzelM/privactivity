package net.privactivity.store.usecase.importactivities;

import net.privactivity.domain.Activity;
import net.privactivity.fit.decode.FitDecoderImpl;
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



import static org.assertj.core.api.Assertions.assertThat;

class ImportActivitiesUseCaseTest {

    @TempDir
    Path importDirectory;

    @Test
    void importDirectory_shouldImportSinglePlainFitFile() throws Exception {
        Path fitFile = importDirectory.resolve("2026-04-04-04-04-44.fit");
        copyToImportDir(fitFile);
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of());

        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                                                                     null, null, _ -> List.of());

        testee.importDirectory(importDirectory, false);

        long expectedId = LocalDateTime.of(2026, 4, 4, 4, 4, 44)
                                       .atZone(ZoneId.systemDefault())
                                       .toInstant()
                                       .toEpochMilli();
        assertThat(activityRepository.getAll(false))
                .extracting(Activity::id)
                .containsExactly(expectedId);
    }

    @Test
    void importDirectory_shouldRemoveExistingActivitiesBeforeImportWhenRequested() throws Exception {
        Path fitFile = importDirectory.resolve("2026-04-04-04-04-44.fit");
        copyToImportDir(fitFile);
        Activity existingActivity = new Activity(1L, "Existing activity", ZonedDateTime.now(), List.of(), Duration.ZERO);
        MapBasedActivityRepository activityRepository =
                new MapBasedActivityRepository(Map.of(existingActivity.id(), existingActivity));

        ImportActivitiesUseCase testee = new ImportActivitiesUseCase(activityRepository, new FitDecoderImpl(),
                                                                     null, null, _ -> List.of());

        testee.importDirectory(importDirectory, true);

        long expectedId = LocalDateTime.of(2026, 4, 4, 4, 4, 44)
                                       .atZone(ZoneId.systemDefault())
                                       .toInstant()
                                       .toEpochMilli();
        assertThat(activityRepository.getAll(false))
                .extracting(Activity::id)
                .containsExactly(expectedId);
    }

    private void copyToImportDir(Path fitFile) throws IOException {
        try (InputStream fixture = Objects.requireNonNull(getClass().getResourceAsStream("/Activity.fit"))) {
            Files.copy(fixture, fitFile);
        }
    }
}
