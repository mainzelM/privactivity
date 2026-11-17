package net.privactivity.fitgenerator;

import com.garmin.fit.ActivityMesg;
import com.garmin.fit.DateTime;
import com.garmin.fit.DeviceIndex;
import com.garmin.fit.DeviceInfoMesg;
import com.garmin.fit.Event;
import com.garmin.fit.EventMesg;
import com.garmin.fit.EventType;
import com.garmin.fit.File;
import com.garmin.fit.FileEncoder;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.Fit;
import com.garmin.fit.GarminProduct;
import com.garmin.fit.LapMesg;
import com.garmin.fit.Manufacturer;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.Sport;
import com.garmin.fit.SportMesg;
import com.garmin.fit.SubSport;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Writes an activity file as a Garmin Edge 1040 would: one lap, one session, 1 Hz records.
 */
public class FitWriter {

    private static final double SEMICIRCLES_PER_DEGREE = 2_147_483_648.0 / 180.0;
    private static final long EDGE_1040_SERIAL = 3_412_345_678L;

    private final ZoneId localZone;

    public FitWriter(ZoneId localZone) {
        this.localZone = localZone;
    }

    public void write(Path file, Instant start, List<RideRecord> records, SessionSummary summary,
                      RiderProfile rider) {
        Instant end = start.plusSeconds((long) summary.durationSeconds());
        FileEncoder encoder = new FileEncoder(file.toFile(), Fit.ProtocolVersion.V2_0);
        try {
            encoder.write(fileId(start));
            encoder.write(deviceInfo(start));
            encoder.write(sport());
            encoder.write(timerEvent(start, EventType.START));
            for (RideRecord r : records) {
                encoder.write(record(start, r));
            }
            encoder.write(timerEvent(end, EventType.STOP_ALL));
            encoder.write(lap(start, end, records, summary));
            encoder.write(session(start, end, records, summary, rider));
            encoder.write(activity(end, summary));
        } finally {
            encoder.close();
        }
    }

    private FileIdMesg fileId(Instant start) {
        FileIdMesg mesg = new FileIdMesg();
        mesg.setType(File.ACTIVITY);
        mesg.setManufacturer(Manufacturer.GARMIN);
        mesg.setProduct(GarminProduct.EDGE_1040);
        mesg.setSerialNumber(EDGE_1040_SERIAL);
        mesg.setTimeCreated(dateTime(start));
        return mesg;
    }

    private DeviceInfoMesg deviceInfo(Instant start) {
        DeviceInfoMesg mesg = new DeviceInfoMesg();
        mesg.setTimestamp(dateTime(start));
        mesg.setDeviceIndex(DeviceIndex.CREATOR);
        mesg.setManufacturer(Manufacturer.GARMIN);
        mesg.setProduct(GarminProduct.EDGE_1040);
        mesg.setSerialNumber(EDGE_1040_SERIAL);
        mesg.setSoftwareVersion(20.22f);
        return mesg;
    }

    private SportMesg sport() {
        SportMesg mesg = new SportMesg();
        mesg.setSport(Sport.CYCLING);
        mesg.setSubSport(SubSport.ROAD);
        return mesg;
    }

    private EventMesg timerEvent(Instant time, EventType type) {
        EventMesg mesg = new EventMesg();
        mesg.setTimestamp(dateTime(time));
        mesg.setEvent(Event.TIMER);
        mesg.setEventType(type);
        mesg.setEventGroup((short) 0);
        return mesg;
    }

    private RecordMesg record(Instant start, RideRecord r) {
        RecordMesg mesg = new RecordMesg();
        mesg.setTimestamp(dateTime(start.plusSeconds(r.secondsSinceStart())));
        mesg.setPositionLat(semicircles(r.position().lat()));
        mesg.setPositionLong(semicircles(r.position().lon()));
        mesg.setDistance((float) r.distanceMeters());
        mesg.setEnhancedSpeed((float) r.speedMetersPerSecond());
        mesg.setEnhancedAltitude((float) r.altitudeMeters());
        mesg.setHeartRate((short) r.heartRate());
        mesg.setCadence((short) r.cadence());
        mesg.setPower(r.power());
        mesg.setTemperature((byte) r.temperatureCelsius());
        mesg.setGrade((float) r.gradePercent());
        return mesg;
    }

    private LapMesg lap(Instant start, Instant end, List<RideRecord> records, SessionSummary s) {
        LapMesg mesg = new LapMesg();
        mesg.setMessageIndex(0);
        mesg.setTimestamp(dateTime(end));
        mesg.setStartTime(dateTime(start));
        mesg.setEvent(Event.LAP);
        mesg.setEventType(EventType.STOP);
        mesg.setSport(Sport.CYCLING);
        mesg.setSubSport(SubSport.ROAD);
        mesg.setTotalElapsedTime((float) s.durationSeconds());
        mesg.setTotalTimerTime((float) s.durationSeconds());
        mesg.setTotalDistance((float) s.distanceMeters());
        mesg.setStartPositionLat(semicircles(records.getFirst().position().lat()));
        mesg.setStartPositionLong(semicircles(records.getFirst().position().lon()));
        RideRecord last = records.getLast();
        mesg.setEndPositionLat(semicircles(last.position().lat()));
        mesg.setEndPositionLong(semicircles(last.position().lon()));
        return mesg;
    }

    private SessionMesg session(Instant start, Instant end, List<RideRecord> records, SessionSummary s,
                                RiderProfile rider) {
        SessionMesg mesg = new SessionMesg();
        mesg.setMessageIndex(0);
        mesg.setTimestamp(dateTime(end));
        mesg.setStartTime(dateTime(start));
        mesg.setEvent(Event.SESSION);
        mesg.setEventType(EventType.STOP);
        mesg.setSport(Sport.CYCLING);
        mesg.setSubSport(SubSport.ROAD);
        mesg.setFirstLapIndex(0);
        mesg.setNumLaps(1);

        mesg.setTotalElapsedTime((float) s.durationSeconds());
        mesg.setTotalTimerTime((float) s.durationSeconds());
        mesg.setTotalMovingTime((float) s.durationSeconds());
        mesg.setTotalDistance((float) s.distanceMeters());
        mesg.setTotalAscent(s.ascentMeters());
        mesg.setTotalDescent(s.descentMeters());
        mesg.setTotalCalories(s.calories());

        mesg.setEnhancedAvgSpeed((float) s.avgSpeedMetersPerSecond());
        mesg.setEnhancedMaxSpeed((float) s.maxSpeedMetersPerSecond());
        mesg.setAvgHeartRate((short) s.avgHeartRate());
        mesg.setMaxHeartRate((short) s.maxHeartRate());
        mesg.setAvgCadence((short) s.avgCadence());
        mesg.setMaxCadence((short) s.maxCadence());
        mesg.setAvgPower(s.avgPower());
        mesg.setMaxPower(s.maxPower());
        mesg.setNormalizedPower(s.normalizedPower());
        mesg.setThresholdPower(rider.ftpWatts());
        mesg.setEnhancedAvgAltitude((float) s.avgAltitudeMeters());
        mesg.setEnhancedMaxAltitude((float) s.maxAltitudeMeters());
        mesg.setAvgTemperature((byte) s.avgTemperature());
        mesg.setMaxTemperature((byte) s.maxTemperature());

        mesg.setIntensityFactor((float) s.intensityFactor());
        mesg.setTrainingStressScore((float) s.trainingStressScore());
        mesg.setTotalTrainingEffect((float) s.totalTrainingEffect());
        mesg.setAvgLeftPco((byte) rider.leftPcoMm());
        mesg.setAvgRightPco((byte) rider.rightPcoMm());

        mesg.setStartPositionLat(semicircles(records.getFirst().position().lat()));
        mesg.setStartPositionLong(semicircles(records.getFirst().position().lon()));
        return mesg;
    }

    private ActivityMesg activity(Instant end, SessionSummary s) {
        ActivityMesg mesg = new ActivityMesg();
        mesg.setTimestamp(dateTime(end));
        mesg.setTotalTimerTime((float) s.durationSeconds());
        mesg.setNumSessions(1);
        mesg.setType(com.garmin.fit.Activity.MANUAL);
        mesg.setEvent(Event.ACTIVITY);
        mesg.setEventType(EventType.STOP);
        mesg.setLocalTimestamp(dateTime(end).getTimestamp()
                               + localZone.getRules().getOffset(end).getTotalSeconds());
        return mesg;
    }

    private DateTime dateTime(Instant instant) {
        return new DateTime(Date.from(instant));
    }

    private int semicircles(double degrees) {
        return (int) Math.round(degrees * SEMICIRCLES_PER_DEGREE);
    }
}
