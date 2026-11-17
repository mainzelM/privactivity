package net.privactivity.fitgenerator;

import net.privactivity.domain.Activity;
import net.privactivity.fit.decode.FitDecoderImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FitGeneratorTest {

    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    private final Geocoder geocoder = name -> switch (name) {
        case "Alpha" -> new Place("Alpha, Land", new GeoPoint(48.0, 11.0));
        case "Omega" -> new Place("Omega, Land", new GeoPoint(48.2, 11.1));
        default -> throw new GenerationException("unknown " + name);
    };

    // ~23 km straight route with hilly elevation
    private final RouteProvider straightRoute = (start, end) -> {
        List<GeoPoint> points = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            points.add(start.interpolate(end, i / 100.0));
        }
        return points;
    };

    private final ElevationProvider hillyElevation = points -> {
        List<Double> elevations = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            elevations.add(500 + 150 * Math.sin(i / 60.0) + 30 * Math.sin(i / 7.0));
        }
        return elevations;
    };

    private FitGenerator testee(RouteProvider routeProvider) {
        return new FitGenerator(geocoder, routeProvider, hillyElevation, new RiderProfile(), BERLIN);
    }

    private GenerationRequest request(Path dir) {
        return new GenerationRequest("Alpha", "Omega", LocalDate.of(2026, 5, 1), LocalTime.of(8, 30, 15), 7, dir);
    }

    @Test
    void nameFileLikeImporterExpects(@TempDir Path dir) {
        Path file = testee(straightRoute).generate(request(dir));

        assertThat(file.getFileName().toString()).isEqualTo("2026-05-01-08-30-15.fit");
        assertThat(file).exists();
    }

    @Test
    void roundTripFillsAllActivityFieldsExceptClimbs(@TempDir Path dir) throws IOException {
        Path file = testee(straightRoute).generate(request(dir));

        Activity activity;
        try (InputStream in = Files.newInputStream(file)) {
            activity = new FitDecoderImpl().extractFit(in);
        }

        assertThat(activity.start().toInstant())
                .isEqualTo(LocalDate.of(2026, 5, 1).atTime(8, 30, 15).atZone(BERLIN).toInstant());
        assertThat(activity.waypoints()).hasSizeGreaterThan(600);
        assertThat(activity.waypoints()).allSatisfy(w -> {
            assertThat(w.latlon().isPresent()).isTrue();
            assertThat(w.speedInMeterPerHour().isPresent()).isTrue();
            assertThat(w.power().isPresent()).isTrue();
            assertThat(w.heartRate().isPresent()).isTrue();
            assertThat(w.cadence().isPresent()).isTrue();
            assertThat(w.altitude().isPresent()).isTrue();
            assertThat(w.distanceInMeter().isPresent()).isTrue();
            assertThat(w.lap()).isEqualTo(1);
        });

        Activity.Totals totals = activity.totals();
        assertThat(totals.distanceInMeters().orElse(0)).isBetween(22_000, 25_000);
        assertThat(totals.ascent().orElse(0)).isGreaterThan(100);
        assertThat(totals.descent().orElse(0)).isGreaterThan(100);
        assertThat(totals.movingTime().isPresent()).isTrue();
        assertThat(totals.movingTime().orElse(Duration.ZERO)).isBetween(Duration.ofMinutes(30), Duration.ofHours(3));
        assertThat(totals.calories().orElse(0)).isGreaterThan(100);

        Activity.Averages averages = activity.averages();
        assertThat(averages.speedInMetersPerHour().orElse(0)).isBetween(15_000, 40_000);
        assertThat(averages.cadence().orElse(0)).isBetween(70, 100);
        assertThat(averages.heartRate().orElse(0)).isBetween(110, 175);
        assertThat(averages.power().orElse(0)).isBetween(120, 250);

        Activity.Maxima maxima = activity.maxima();
        assertThat(maxima.speedInMetersPerHour().orElse(0)).isGreaterThan(averages.speedInMetersPerHour().orElse(0));
        assertThat(maxima.heartRate().orElse(0)).isGreaterThan(averages.heartRate().orElse(0));
        assertThat(maxima.power().orElse(0)).isGreaterThan(averages.power().orElse(0));
        assertThat(maxima.altitude().orElse(0)).isBetween(500, 700);

        Activity.Summary summary = activity.summary();
        assertThat(summary.trainingStressScore().isPresent()).isTrue();
        assertThat(summary.totalTrainingEffect().isPresent()).isTrue();
        assertThat(summary.intensityFactor().isPresent()).isTrue();
        assertThat(summary.avgLeftPco().isPresent()).isTrue();
        assertThat(summary.avgRightPco().isPresent()).isTrue();
        assertThat(activity.climbPointers()).isEmpty();
    }

    @Test
    void generateSameFileForSameSeed(@TempDir Path dir) throws IOException {
        Path first = testee(straightRoute).generate(request(dir.resolve("a")));
        Path second = testee(straightRoute).generate(request(dir.resolve("b")));

        assertThat(Files.readAllBytes(first)).isEqualTo(Files.readAllBytes(second));
    }

    @Test
    void rejectRoutesLongerThan200Km(@TempDir Path dir) {
        RouteProvider longRoute = (start, _) -> List.of(start, new GeoPoint(start.lat() + 2.0, start.lon()));

        assertThatThrownBy(() -> testee(longRoute).generate(request(dir)))
                .isInstanceOf(GenerationException.class)
                .hasMessageContaining("maximum is 200 km");
    }
}
