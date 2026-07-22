package net.privactivity.tcx;

import com.garmin.xmlschemas.trainingcenterdatabase.v2.ActivityLapT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.ActivityT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.HeartRateInBeatsPerMinuteT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.PositionT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.TrackT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.TrackpointT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.TrainingCenterDatabaseT;
import net.privactivity.domain.Activity;
import net.privactivity.domain.Bounds;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.usecase.importactivities.adapter.TcxImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TcxImporterImpl implements TcxImporter {
    private static final Logger logger = LoggerFactory.getLogger(TcxImporterImpl.class);

    @Override
    public Activity importTcx(InputStream is, long activityId) {
        var parser = new TcxXmlUtil();
        TrainingCenterDatabaseT tcx = parser.unmarshall(is);
        String title = "TCX " + activityId + " from " + tcx.getAuthor().getName();
        if (tcx.getActivities().getActivity().size() > 1) {
            logger.warn("Found multiple activities, taking first {}", activityId);
        }
        ActivityT activityT = tcx.getActivities().getActivity().getFirst();
        return fromSingleTcxActivity(activityId, title, activityT);
    }

    private Activity fromSingleTcxActivity(long activityId, String title, ActivityT activityT) {
        ZonedDateTime start = activityT.getId().atZone(ZoneId.systemDefault());

        if (activityT.getLap().isEmpty()) {
            throw new RuntimeException("TCX without laps " + activityId);
        }

        List<ActivityLapT> allLaps = activityT.getLap();

        List<Waypoint> waypoints = getWaypoints(allLaps, start);
        Maybe<Duration> movingTime = getMovingTime(allLaps);

        Activity.Totals totales = new Activity.Totals(
                getDistance(allLaps),
                Maybe.none(), // ascent
                Maybe.none(), // descent
                movingTime,
                getCalories(allLaps));
        Activity.Averages averages = new Activity.Averages(
                Maybe.none(), // speed
                getCadence(allLaps),
                getAvgHeartRate(allLaps),
                Maybe.none()); // power
        Activity.Maxima maxima = new Activity.Maxima(
                Maybe.none(), // speed
                getMaxHeartRate(allLaps),
                Maybe.none(), // power
                Maybe.none()); // altitude
        Bounds bounds = Bounds.ofWaypoints(waypoints);
        return Activity.builder()
                       .id(activityId)
                       .title(title)
                       .start(start)
                       .waypoints(waypoints)
                       .totals(totales)
                       .averages(averages)
                       .maxima(maxima)
                       .climbPointers(List.of())
                       .bounds(bounds)
                       .build();
    }

    private static Maybe<Integer> getDistance(List<ActivityLapT> activityLaps) {
        return Maybe.fromOptional(activityLaps.stream()
                                              .map(ActivityLapT::getDistanceMeters)
                                              .reduce(Double::sum)
                                              .map(Math::round)
                                              .map(Long::intValue));
    }

    private static Maybe<Integer> getCalories(List<ActivityLapT> activityLaps) {
        return Maybe.fromOptional(activityLaps.stream()
                                              .map(ActivityLapT::getCalories)
                                              .reduce(Integer::sum)
                                              .map(Math::round));
    }

    private static Maybe<Duration> getMovingTime(List<ActivityLapT> activityLaps) {
        return Maybe.fromOptional(activityLaps.stream()
                                              .map(ActivityLapT::getTotalTimeSeconds)
                                              .reduce(Double::sum)
                                              .map(Math::round)
                                              .map(Duration::ofSeconds));
    }

    private Maybe<Integer> getCadence(List<ActivityLapT> activityLaps) {
        double avg = activityLaps.stream()
                                 .map(ActivityLapT::getCadence)
                                 .filter(Objects::nonNull)
                                 .mapToDouble(cad -> (double) cad)
                                 .summaryStatistics().getAverage();
        if (avg == 0.0) {
            return Maybe.none();
        } else {
            return Maybe.some((int) Math.round(avg));
        }
    }

    private Maybe<Integer> getMaxHeartRate(List<ActivityLapT> activityLaps) {
        return Maybe.fromOptional(getHeartRates(activityLaps)
                                          .mapToInt(hr -> (int) hr)
                                          .max());
    }

    private Maybe<Integer> getAvgHeartRate(List<ActivityLapT> activityLaps) {
        double avg = getHeartRates(activityLaps)
                .mapToDouble(hr -> (double) hr)
                .summaryStatistics().getAverage();
        if (avg == 0.0) {
            return Maybe.none();
        } else {
            return Maybe.some((int) Math.round(avg));
        }
    }

    private static Stream<Short> getHeartRates(List<ActivityLapT> activityLaps) {
        return activityLaps.stream()
                           .map(ActivityLapT::getMaximumHeartRateBpm)
                           .filter(Objects::nonNull)
                           .map(HeartRateInBeatsPerMinuteT::getValue)
                           .filter(hr -> hr > 70);
    }

    private List<Waypoint> getWaypoints(List<ActivityLapT> activityLaps, ZonedDateTime start) {
        return activityLaps.stream()
                           .flatMap(lap -> lap.getTrack().stream())
                           .flatMap(trackT -> trackToWaypoints(start, trackT))
                           .collect(Collectors.toList());

    }

    private Stream<Waypoint> trackToWaypoints(ZonedDateTime start, TrackT trackT) {
        return trackT.getTrackpoint().stream()
                     .filter(tp -> tp.getPosition() != null)
                     .map(tp -> trackPointToWayPoint(tp, start));
    }

    private Waypoint trackPointToWayPoint(TrackpointT trackpointT, ZonedDateTime start) {
        int secSinceStart = (int) Duration.between(start.toInstant(), trackpointT.getTime()).toSeconds();
        PositionT position = trackpointT.getPosition();
        return new Waypoint(secSinceStart, Maybe.some(new LatLon(position.getLatitudeDegrees(),
                                                                 position.getLongitudeDegrees())), Maybe.none(),
                            Maybe.none(), getHeartRate(trackpointT.getHeartRateBpm()),
                            Maybe.nullAsNone(trackpointT.getCadence()).map(Short::intValue),
                            Maybe.some(Math.toIntExact(Math.round(trackpointT.getAltitudeMeters()))),
                            Maybe.some(Math.toIntExact(Math.round(trackpointT.getDistanceMeters()))), 1);
    }

    private static Maybe<Integer> getHeartRate(HeartRateInBeatsPerMinuteT heartRateInBeatsPerMinuteT) {
        return Maybe.nullAsNone(heartRateInBeatsPerMinuteT)
                    .map(HeartRateInBeatsPerMinuteT::getValue)
                    .map(Short::intValue);

    }
}
