package net.privactivity.store.usecase.export.exportascsv;

import net.privactivity.domain.Activity;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.model.MapBasedActivityRepository;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class ExportAsCsvUseCaseTest {

    @Test
    void testExportActivityAsCsv_AveragesWaypoints() {
        ActivityRepository activityRepository = activitiesWithTwoWindows();
        ExportAsCsvUseCase testee = new ExportAsCsvUseCase(activityRepository);
        CsvExportConfig csvExportConfig =
                new CsvExportConfig(false, false, true, true, false, false, false,
                                    60);

        CsvExportResult result = testee.exportActivityAsCsv(1L, csvExportConfig);
        String[] lines = result.csvContent().split("\n");

        assertThat(result.fileName()).isEqualTo("activity-1-2024-03-14.csv");
        assertThat(lines[0]).isEqualTo("Time,Power,HeartRate");
        assertThat(lines[1]).isEqualTo("0,250,150");
        assertThat(lines[2]).isEqualTo("60,250,160");
    }

    private static ActivityRepository activitiesWithTwoWindows() {
        // Create waypoints in two windows (0-59s and 60-119s)
        List<Waypoint> waypoints = List.of(
                // Window 1: Average Power (200+300)/2 = 250, HeartRate (140+160)/2 = 150
                new Waypoint(0, Maybe.some(new LatLon(50.0, 10.0)), Maybe.none(), Maybe.some(200), Maybe.some(140),
                             Maybe.none(), Maybe.none(), Maybe.none(), 1),
                new Waypoint(30, Maybe.some(new LatLon(50.1, 10.1)), Maybe.none(), Maybe.some(300), Maybe.some(160),
                             Maybe.none(), Maybe.none(), Maybe.none(), 1),

                // Window 2: Average Power (100+400)/2 = 250, HeartRate (150+170)/2 = 160
                new Waypoint(60, Maybe.some(new LatLon(51.0, 11.0)), Maybe.none(), Maybe.some(100), Maybe.some(150),
                             Maybe.none(), Maybe.none(), Maybe.none(), 1),
                new Waypoint(90, Maybe.some(new LatLon(51.1, 11.1)), Maybe.none(), Maybe.some(400), Maybe.some(170),
                             Maybe.none(), Maybe.none(), Maybe.none(), 1)
                                          );

        Activity activity = Activity.builder()
                                    .id(1L)
                                    .title("Test Activity")
                                    .start(ZonedDateTime.parse("2024-03-14T10:15:30+01:00"))
                                    .waypoints(waypoints)
                                    .totals(new Activity.Totals(Duration.ZERO))
                                    .build();

        return new MapBasedActivityRepository(Map.of(1L, activity));
    }

    @Test
    void testExportActivityAsCsv_HandlesMissingValues() {
        ExportAsCsvUseCase testee = new ExportAsCsvUseCase(activityWithMissingValues());
        CsvExportConfig csvExportConfig =
                new CsvExportConfig(false, false, true, true, false, false, false,
                                    60);

        CsvExportResult result = testee.exportActivityAsCsv(1L, csvExportConfig);
        String[] lines = result.csvContent().split("\n");

        assertThat(result.fileName()).isEqualTo("activity-1-2024-03-14.csv");
        assertThat(lines[1]).isEqualTo("0,200,");
    }

    private static ActivityRepository activityWithMissingValues() {
        List<Waypoint> waypoints = List.of(
                // Window 1: Only one has power (200), other is none -> Average = 200/1 = 200
                new Waypoint(0, Maybe.none(), Maybe.none(), Maybe.some(200), Maybe.none(), Maybe.none(), Maybe.none()
                        , Maybe.none(), 1),
                new Waypoint(30, Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(),
                             Maybe.none(), 1)
                                          );

        Activity activity = Activity.builder()
                                    .id(1L)
                                    .title("Test Activity")
                                    .start(ZonedDateTime.parse("2024-03-14T10:15:30+01:00"))
                                    .waypoints(waypoints)
                                    .totals(new Activity.Totals(Duration.ZERO))
                                    .build();

        return new MapBasedActivityRepository(Map.of(1L, activity));
    }

    @Test
    void testExportActivityAsCsv_IncludesLapWhenConfigured() {
        ExportAsCsvUseCase testee = new ExportAsCsvUseCase(activitiesWithTwoWindows());
        CsvExportConfig csvExportConfig =
                new CsvExportConfig(false, false, false, false, false, false, true,
                                    60);

        CsvExportResult result = testee.exportActivityAsCsv(1L, csvExportConfig);
        String[] lines = result.csvContent().split("\n");

        assertThat(lines[0]).isEqualTo("Time,Lap");
        assertThat(lines[1]).isEqualTo("0,1");
        assertThat(lines[2]).isEqualTo("60,1");
    }

    @Test
    void testExportActivityAsCsv_AveragesLapWhenConfigured() {
        ExportAsCsvUseCase testee = new ExportAsCsvUseCase(activitiesWithMixedLaps());
        CsvExportConfig csvExportConfig =
                new CsvExportConfig(false, false, false, false, false, false, true,
                                    60);

        CsvExportResult result = testee.exportActivityAsCsv(1L, csvExportConfig);
        String[] lines = result.csvContent().split("\n");

        assertThat(lines[0]).isEqualTo("Time,Lap");
        assertThat(lines[1]).isEqualTo("0,2");
        assertThat(lines[2]).isEqualTo("60,3");
    }

    private static ActivityRepository activitiesWithMixedLaps() {
        List<Waypoint> waypoints = List.of(
                new Waypoint(0, Maybe.some(new LatLon(50.0, 10.0)), Maybe.none(), Maybe.none(), Maybe.none(),
                             Maybe.none(), Maybe.none(), Maybe.none(), 1),
                new Waypoint(30, Maybe.some(new LatLon(50.1, 10.1)), Maybe.none(), Maybe.none(), Maybe.none(),
                             Maybe.none(), Maybe.none(), Maybe.none(), 2),
                new Waypoint(60, Maybe.some(new LatLon(51.0, 11.0)), Maybe.none(), Maybe.none(), Maybe.none(),
                             Maybe.none(), Maybe.none(), Maybe.none(), 2),
                new Waypoint(90, Maybe.some(new LatLon(51.1, 11.1)), Maybe.none(), Maybe.none(), Maybe.none(),
                             Maybe.none(), Maybe.none(), Maybe.none(), 3)
                                           );

        Activity activity = Activity.builder()
                                    .id(1L)
                                    .title("Test Activity")
                                    .start(ZonedDateTime.parse("2024-03-14T10:15:30+01:00"))
                                    .waypoints(waypoints)
                                    .totals(new Activity.Totals(Duration.ZERO))
                                    .build();

        return new MapBasedActivityRepository(Map.of(1L, activity));
    }
}