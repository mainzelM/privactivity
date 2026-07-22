package net.privactivity.gpx;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Latitude;
import io.jenetics.jpx.Length;
import io.jenetics.jpx.Longitude;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import net.privactivity.domain.Activity;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.usecase.importactivities.adapter.GpxImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class GpxImporterImpl implements GpxImporter {
    private static final Logger logger = LoggerFactory.getLogger(GpxImporterImpl.class);

    @Override
    public Activity importGpxAsActivity(InputStream is, long id) {
        try {
            GPX read = GPX.Reader.of(GPX.Version.V11, GPX.Reader.Mode.LENIENT).read(is);
            List<Track> tracks = read.getTracks();
            if (tracks.isEmpty()) {
                throw new RuntimeException("No track found");
            }
            if (tracks.size() > 1) {
                logger.warn("More than one track");
            }
            Track t = read.getTracks().getFirst();

            String title = t.getName().orElse("No name in GPX");
            List<TrackSegment> segments = t.getSegments();
            ZonedDateTime start = segments.getFirst().getPoints().getFirst().getTime().orElseThrow()
                                          .atZone(ZoneId.systemDefault());
            List<Waypoint> waypoints = segments.stream()
                                               .flatMap(seg -> seg.getPoints().stream()
                                                                  .map(wp -> gpxWpToWaypoint(wp, start)))
                                               .collect(Collectors.toList());
            Waypoint lastWaypoint = waypoints.getLast();
            Duration duration = Duration.ofSeconds(lastWaypoint.secondsSinceStart());

            return Activity.builder()
                           .id(id)
                           .title(title)
                           .start(start)
                           .waypoints(waypoints)
                           .totals(new Activity.Totals(duration))
                           .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Waypoint gpxWpToWaypoint(io.jenetics.jpx.WayPoint wayPoint, ZonedDateTime start) {
        int secSinceStart = (int) Duration.between(start.toInstant(), wayPoint.getTime().orElseThrow()).toSeconds();
        Latitude latitude = wayPoint.getLatitude();
        Longitude longitude = wayPoint.getLongitude();
        LatLon latLon = new LatLon(latitude.doubleValue(), longitude.doubleValue());
        Maybe<Integer> altitude = Maybe.fromOptional(wayPoint.getElevation().map(Length::intValue));
        return new Waypoint(secSinceStart, Maybe.some(latLon), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none()
                , altitude, Maybe.none(), 1);
    }
}
