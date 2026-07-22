package net.privactivity.gpx;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Bounds;
import net.privactivity.domain.LatLon;
import net.privactivity.store.usecase.importactivities.adapter.GpxImporter;
import org.junit.jupiter.api.Test;
import java.io.InputStream;



import static org.assertj.core.api.Assertions.assertThat;

class GpxImporterTest {

    @Test
    void test() {
        GpxImporter importer = new GpxImporterImpl();
        InputStream gpxStream = getClass().getResourceAsStream("import.gpx");
        assertThat(gpxStream).isNotNull();
        Activity activity = importer.importGpxAsActivity(gpxStream, 23L);
        assertThat(activity.id()).isEqualTo(23L);
        assertThat(activity.title()).isEqualTo("Track 044");
        assertThat(activity.totals().movingTime().orThrow().getSeconds()).isEqualTo(2220L);
        assertThat(activity.start().getYear()).isEqualTo(2000);
        assertThat(activity.waypoints()).isNotEmpty();
        Bounds expected = new Bounds(
                new LatLon(49.9504894, 7.9937189),
                new LatLon(49.9602641, 7.9555655),
                new LatLon(49.9602641, 7.9555655),
                new LatLon(49.9509132, 7.9998346));
        assertThat(activity.bounds()).isEqualTo(expected);
    }

}