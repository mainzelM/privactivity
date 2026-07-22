package net.privactivity.fit.decode;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Waypoint;
import net.privactivity.fit.domain.Record;
import net.privactivity.fit.domain.TrainingData;
import org.junit.jupiter.api.Test;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Objects;



import static org.assertj.core.api.Assertions.assertThat;

class FitToPrivactivityTest {

    @Test
    void testConvertActivityFixture() throws URISyntaxException {
        FitDecoderImpl decoder = new FitDecoderImpl();
        Path fitFilePath = Path.of(Objects.requireNonNull(getClass().getResource("/Activity.fit")).toURI());
        TrainingData trainingData = decoder.decode(fitFilePath.toString());
        trainingData.setId(23L);

        FitToPrivactivity testee = new FitToPrivactivity();
        Activity activity = testee.convert(trainingData);

        List<Waypoint> waypoints = activity.waypoints();
        Waypoint firstWaypoint = waypoints.getFirst();
        Waypoint lastWaypoint = waypoints.getLast();

        assertThat(activity.id()).isEqualTo(23L);
        assertThat(activity.title()).isEqualTo("title");
        assertThat(activity.start().toInstant()).isEqualTo(trainingData.getDate().toInstant());
        assertThat(waypoints).hasSize(3601);

        assertThat(firstWaypoint.secondsSinceStart()).isEqualTo(0);
        assertThat(firstWaypoint.speedInMeterPerHour().orThrow()).isEqualTo(3600);
        assertThat(firstWaypoint.power().orThrow()).isEqualTo(150);
        assertThat(firstWaypoint.heartRate().orThrow()).isEqualTo(126);
        assertThat(firstWaypoint.cadence().orThrow()).isEqualTo(0);
        assertThat(firstWaypoint.altitude().orThrow()).isEqualTo(127);
        assertThat(firstWaypoint.distanceInMeter().orThrow()).isEqualTo(0);
        assertThat(firstWaypoint.lap()).isEqualTo(1);

        assertThat(lastWaypoint.secondsSinceStart()).isEqualTo(3600);
        assertThat(lastWaypoint.speedInMeterPerHour().orThrow()).isEqualTo(3600);
        assertThat(lastWaypoint.power().orThrow()).isEqualTo(150);
        assertThat(lastWaypoint.heartRate().orThrow()).isEqualTo(126);
        assertThat(lastWaypoint.cadence().orThrow()).isEqualTo(30);
        assertThat(lastWaypoint.altitude().orThrow()).isEqualTo(97);
        assertThat(lastWaypoint.distanceInMeter().orThrow()).isEqualTo(3600);
        assertThat(lastWaypoint.lap()).isEqualTo(1);

        assertThat(activity.totals().movingTime().isPresent()).isFalse();
    }

    @Test
    void testConvertRecordWithoutLapNumberDefaultsToOne() {
        Record record = new Record();
        Date timestamp = new Date();
        record.setTimestamp(timestamp);

        TrainingData trainingData = new TrainingData();
        trainingData.setDate(timestamp);
        trainingData.addRecord(record);

        FitToPrivactivity testee = new FitToPrivactivity();
        Activity activity = testee.convert(trainingData);

        assertThat(activity.waypoints()).singleElement().satisfies(waypoint -> assertThat(waypoint.lap()).isEqualTo(1));
    }
}
