package net.privactivity.fit.decode;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Bounds;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.None;
import net.privactivity.domain.Some;
import net.privactivity.domain.Waypoint;
import net.privactivity.fit.domain.GarminSession;
import net.privactivity.fit.domain.Record;
import net.privactivity.fit.domain.TrainingData;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;



import static net.privactivity.domain.Maybe.nullAsNone;

public class FitToPrivactivity {

    public Activity convert(TrainingData td) {
        List<Waypoint> waypoints = convertRecords(td);
        return convertFromWaypoints(td, waypoints);
    }

    private Activity convertFromWaypoints(TrainingData td, List<Waypoint> waypoints) {
        Instant startInstant = getInstant(td);
        ZonedDateTime start = ZonedDateTime.ofInstant(startInstant, ZoneId.systemDefault());
        GarminSession gs = td.getGarminSession();
        if (gs == null) {
            return Activity.builder()
                           .id(td.getId())
                           .title(td.getTitle())
                           .start(start)
                           .waypoints(waypoints)
                           .totals(new Activity.Totals(Duration.ZERO))
                           .build();
        } else {
            Activity.Totals totals = makeTotals(gs);
            Activity.Averages averages = makeAverages(gs);
            Activity.Maxima maxima = makeMaxima(gs);
            Activity.Summary summary = makeSummary(gs);
            Bounds bounds = Bounds.ofWaypoints(waypoints);
            return Activity.builder()
                           .id(td.getId())
                           .title(td.getTitle())
                           .start(start)
                           .waypoints(waypoints)
                           .totals(totals)
                           .averages(averages)
                           .maxima(maxima)
                           .summary(summary)
                           .climbPointers(Collections.emptyList())
                           .bounds(bounds)
                           .build();
        }
    }

    private List<Waypoint> convertRecords(TrainingData td) {
        Instant startInstant = getInstant(td);
        return td.getRecords().stream()
                 .map(r -> convertRecord(r, startInstant))
                 .collect(Collectors.toList());
    }

    private static Instant getInstant(TrainingData td) {
        Date date = td.getDate();
        if (date == null) {
            return Instant.now();
        } else {
            return date.toInstant();
        }
    }

    private Waypoint convertRecord(Record record, Instant startInstant) {
        int secondsSinceStart = 0;
        if (record.getTimestamp() != null) {
            secondsSinceStart = (int) TimeUnit.MILLISECONDS
                    .toSeconds(record.getTimestamp().toInstant().toEpochMilli() - startInstant.toEpochMilli());
        }
        return new Waypoint(
                secondsSinceStart,
                extractLatLonOrNone(record),
                nullAsNone(record.getSpeed()).map(this::garminSpeedToMeterPerHour),
                nullAsNone(record.getPower()),
                nullAsNone(record.getHeartRate()).map(Short::intValue),
                nullAsNone(record.getCadence()).map(Short::intValue),
                nullAsNone(record.getAltitude()).map(d -> (int) Math.round(d)),
                nullAsNone(record.getDistance()).map(d -> (int) Math.round(d)),
                nullAsNone(record.getLapNumber()).orElse(1));
    }

    private Maybe<LatLon> extractLatLonOrNone(Record record) {
        if (record.getLat() == null || record.getLon() == null) {
            return new None<>();
        } else {
            return new Some<>(new LatLon(record.getLat(), record.getLon()));
        }
    }

    private Activity.Totals makeTotals(GarminSession gs) {
        return new Activity.Totals(nullAsNone(gs.getTotalDistance()).map(d -> (int) Math.round(d)),
                                   nullAsNone(gs.getTotalAscent()),
                                   nullAsNone(gs.getTotalDescent()),
                                   nullAsNone(gs.getTotalMovingTime()).map(d -> (int) Math.round(d)).map(Duration::ofSeconds),
                                   nullAsNone(gs.getTotalCalories()));
    }

    private Activity.Averages makeAverages(GarminSession gs) {
        return new Activity.Averages(nullAsNone(gs.getAvgSpeed()).map(this::garminSpeedToMeterPerHour),
                                     nullAsNone(gs.getAvgCadence()).map(Short::intValue),
                                     nullAsNone(gs.getAvgHeartRate()).map(Short::intValue),
                                     nullAsNone(gs.getAvgPower()));
    }

    private Activity.Maxima makeMaxima(GarminSession gs) {
        return new Activity.Maxima(nullAsNone(gs.getMaxSpeed()).map(this::garminSpeedToMeterPerHour),
                                   nullAsNone(gs.getMaxHeartRate()).map(Short::intValue),
                                   nullAsNone(gs.getMaxPower()),
                                   nullAsNone(gs.getMaxAltitude()).map(d -> (int) Math.round(d)));
    }

    private Activity.Summary makeSummary(GarminSession gs) {
        return new Activity.Summary(nullAsNone(gs.getTrainingStressScore()),
                                    nullAsNone(gs.getTotalTrainingEffect()),
                                    nullAsNone(gs.getIntensityFactor()),
                                    nullAsNone(gs.getAvgLeftPco()),
                                    nullAsNone(gs.getAvgRightPco()));
    }

    private Integer garminSpeedToMeterPerHour(double mPerS) {
        return (int) Math.round(mPerS * 3600);
    }
}
