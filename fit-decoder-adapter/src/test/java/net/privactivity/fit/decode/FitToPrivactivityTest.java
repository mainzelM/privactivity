package net.privactivity.fit.decode;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Waypoint;
import net.privactivity.fit.domain.GarminSession;
import net.privactivity.fit.domain.Record;
import net.privactivity.fit.domain.TrainingData;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.Date;



import static org.assertj.core.api.Assertions.assertThat;

class FitToPrivactivityTest {

    @Test
    void testConvertActivityFixture() {
        Date start = new Date(1_700_000_000_000L);
        TrainingData trainingData = new TrainingData();
        trainingData.setId(23L);
        trainingData.setTitle("Test activity");
        trainingData.setDate(start);
        trainingData.addRecord(recordAt(start, (short) 0, 0.0, 127.0));
        trainingData.addRecord(recordAt(new Date(start.getTime() + 3_600_000L), (short) 30, 3600.0, 97.0));
        GarminSession garminSession = new GarminSession();
        garminSession.setTotalMovingTime(30.0);
        trainingData.setGarminSession(garminSession);

        FitToPrivactivity testee = new FitToPrivactivity();
        Activity activity = testee.convert(trainingData);

        Waypoint firstWaypoint = activity.waypoints().getFirst();
        Waypoint lastWaypoint = activity.waypoints().getLast();

        assertThat(activity.id()).isEqualTo(23L);
        assertThat(activity.start().toInstant()).isEqualTo(trainingData.getDate().toInstant());
        assertThat(activity.waypoints()).hasSize(2);

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

        assertThat(activity.totals().movingTime().orThrow()).isEqualTo(Duration.ofSeconds(30));
    }

    private Record recordAt(Date timestamp, short cadence, double distance, double altitude) {
        Record record = new Record();
        record.setTimestamp(timestamp);
        record.setSpeed(1.0);
        record.setPower(150);
        record.setHeartRate((short) 126);
        record.setCadence(cadence);
        record.setAltitude(altitude);
        record.setDistance(distance);
        record.setLapNumber(1);
        return record;
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
