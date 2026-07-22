package net.privactivity.store.usecase.export.exportasgpx;

import net.privactivity.domain.Activity;
import net.privactivity.domain.HaversineDistance;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.usecase.export.adapter.GpxExporter;



import static java.util.function.Predicate.not;

public class ExportAsGpxUseCase {

    private static final LatLon HOME = new LatLon(47.510766, 8.378093);

    private final ActivityRepository activityRepository;
    private final GpxExporter exporter;

    public ExportAsGpxUseCase(ActivityRepository activityRepository, GpxExporter exporter) {
        this.activityRepository = activityRepository;
        this.exporter = exporter;
    }

    public String export(long activityId, float speedFactor, int startDiffHours) {
        Activity originalActivity = activityRepository.getActivityById(activityId, true);
        Activity strippedActivity = stripDown(originalActivity, speedFactor, startDiffHours);
        return exporter.exportActivityAsGpx(strippedActivity);
    }

    private Activity stripDown(Activity originalActivity, float speedFactor, int startDiffHours) {
        return originalActivity.withWaypoints(originalActivity.waypoints().stream()
                                                              .filter(not(this::closeToHome))
                                                              .map(wp -> stripWaypoint(wp, speedFactor))
                                                              .toList())
                               .withStart(originalActivity.start().plusHours(startDiffHours));
    }

    private boolean closeToHome(Waypoint wp) {
        LatLon wpLatLon = wp.latlon().orElse(HOME);
        return HaversineDistance.distanceInMeter(wpLatLon, HOME) < 700;
    }

    private Waypoint stripWaypoint(Waypoint wp, float speedFactor) {
        return new Waypoint((int) (wp.secondsSinceStart() * speedFactor), wp.latlon(),
                            wp.speedInMeterPerHour().map(s -> (int) (s * speedFactor)), Maybe.none(), Maybe.none(),
                            Maybe.none(), wp.altitude(), wp.distanceInMeter().map(d -> (int) (d * speedFactor)), 1);
    }
}
