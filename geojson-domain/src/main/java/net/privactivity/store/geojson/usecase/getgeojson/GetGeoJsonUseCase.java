package net.privactivity.store.geojson.usecase.getgeojson;

import net.privactivity.domain.Activity;
import net.privactivity.domain.ClimbPointer;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.adapter.ActivityRepository;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class GetGeoJsonUseCase {
    private final ActivityRepository activityRepository;

    public GetGeoJsonUseCase(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public Object asGeoJson(long activityId, List<ClimbPointer> climbs) {
        Activity activity = activityRepository.getActivityById(activityId, true);
        FeatureCollection featureCollection = new FeatureCollection() {
            public String getName() {
                return activity.title();
            }
        };
        LineString p = new LineString();

        List<Waypoint> waypoints = sanitize(activity.waypoints());
        p.setCoordinates(waypoints.stream()
                                  .map(this::wpToLngLatAlt)
                                  .toList());
        Feature feature = new Feature();
        feature.setGeometry(p);
        List<String> times = waypoints.stream()
                                      .map(wp -> wpToTime(wp, activity))
                                      .toList();
        List<Integer> heartRates = waypoints.stream()
                                            .map(this::wpToHr)
                                            .toList();
        List<Integer> powers = waypoints.stream()
                                        .map(this::wpToPower)
                                        .toList();
        feature.setProperties(
                Map.of("heartRates", heartRates,
                       "coordTimes", times,
                       "powers", powers,
                       "time", activity.start().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));

        featureCollection.add(feature);

        climbs.forEach(c -> addClimb(c, activity, featureCollection));

        return featureCollection;
    }

    private List<Waypoint> sanitize(List<Waypoint> waypoints) {
        return waypoints.stream()
                        .filter(wp -> wp.latlon().isPresent())
                        .toList();
    }

    private void addClimb(ClimbPointer climb, Activity activity, FeatureCollection featureCollection) {
        Waypoint wpStart = findWaypointInActivity(climb.startMeters(), activity);
        Feature pointStart = createPointFeature(wpStart);
        pointStart.setProperties(Map.of("name", "Climb start",
                                        "desc", String.format("Ascent: %d, descent: %d, len: %d, alt: %d",
                                                              climb.ascent(), climb.descent(),
                                                              climb.endMeters() - climb.startMeters(),
                                                              wpStart.altitude().orElse(0))));
        featureCollection.add(pointStart);
        Waypoint wpEnd = findWaypointInActivity(climb.endMeters(), activity);
        Feature pointEnd = createPointFeature(wpEnd);
        pointEnd.setProperties(Map.of("name", "Climb end",
                                      "desc", String.format("Alt: %d ", wpEnd.altitude().orElse(0))));
        featureCollection.add(pointEnd);
    }

    private Feature createPointFeature(Waypoint wp) {
        Feature wp1 = new Feature();
        Point p1 = new Point();
        p1.setCoordinates(wpToLngLatAlt(wp));
        wp1.setGeometry(p1);
        return wp1;
    }

    private Waypoint findWaypointInActivity(int startMeters, Activity activity) {
        return activity.waypoints().stream()
                       .filter(wp -> wp.distanceInMeter().orElse(-1) == startMeters)
                       .findAny()
                       .orElseThrow();
    }

    private int wpToHr(Waypoint wp) {
        return wp.heartRate().orElse(0);
    }

    private int wpToPower(Waypoint wp) {
        return wp.power().orElse(0);
    }

    private String wpToTime(Waypoint waypoint, Activity activity) {
        return activity.start()
                       .plusSeconds(waypoint.secondsSinceStart())
                       .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private LngLatAlt wpToLngLatAlt(Waypoint wp) {
        return new LngLatAlt(wp.latlon().map(LatLon::lon).orElse(0.0),
                             wp.latlon().map(LatLon::lat).orElse(0.0),
                             wp.altitude().orElse(0));

    }
}
