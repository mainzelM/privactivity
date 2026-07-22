package net.privactivity.export.gpx;

import net.privactivity.domain.Activity;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;



import static org.assertj.core.api.Assertions.assertThat;

class GpxExporterImplTest {

    @Test
    void exportActivityAsGpxShouldContainCoreTrackData() {
        GpxExporterImpl testee = new GpxExporterImpl();
        ZonedDateTime start = ZonedDateTime.parse("2024-01-02T10:15:30+01:00[Europe/Berlin]");
        Activity activity = new Activity(
                42L,
                "Morning Ride",
                start,
                List.of(
                        waypoint(45.123456, 9.7654321, Maybe.some(500)),
                        waypoint(45.223456, 9.8754321, Maybe.none())),
                Duration.ofMinutes(30));

        String gpx = testee.exportActivityAsGpx(activity);

        assertThat(gpx).contains("creator=\"Strava\"");
        assertThat(gpx).contains("lat=\"45.123456\"");
        assertThat(gpx).contains("lon=\"9.7654321\"");
        assertThat(gpx).contains("lat=\"45.223456\"");
        assertThat(gpx).contains("lon=\"9.8754321\"");
        assertThat(gpx).containsPattern("<ele>500(\\.0+)?</ele>");
        assertThat(gpx).containsPattern("<ele>0(\\.0+)?</ele>");
        assertThat(gpx).contains("<time>2024-01-02T09:15:30Z</time>");
        assertThat(gpx.split("<trkpt ").length - 1).isEqualTo(2);
    }

    private Waypoint waypoint(double lat, double lon, Maybe<Integer> altitude) {
        return new Waypoint(
                0,
                Maybe.some(new LatLon(lat, lon)),
                Maybe.none(),
                Maybe.none(),
                Maybe.none(),
                Maybe.none(),
                altitude,
                Maybe.none(),
                1);
    }
}
