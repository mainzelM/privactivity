package net.privactivity.export.gpx;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import net.privactivity.domain.Activity;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.usecase.export.adapter.GpxExporter;
import java.time.ZonedDateTime;

public class GpxExporterImpl implements GpxExporter {

    @Override
    public String exportActivityAsGpx(Activity activity) {
        GPX gpx =
                GPX.builder()
                   .creator("Strava")
                   .addTrack(track ->
                                     track.addSegment(segment ->
                                                              addPoints(segment, activity)))
                   .build();
        return GPX.Writer.DEFAULT.toString(gpx);

    }

    private void addPoints(TrackSegment.Builder segment, Activity activity) {
        ZonedDateTime activityStart = activity.start();
        activity.waypoints()
                .forEach(wp -> segment.addPoint(point -> addWaypoint(point, wp, activityStart)));
    }


    private void addWaypoint(WayPoint.Builder p, Waypoint wp, ZonedDateTime start) {
        LatLon latLon = wp.latlon().orThrow();
        p.lat(latLon.lat())
         .lon(latLon.lon())
         .time(start.toInstant())
         .ele(wp.altitude().orElse(0));
    }
}
