package net.privactivity.store.geojson.usecase.getgeojson;

import net.privactivity.domain.Activity;
import net.privactivity.domain.ClimbPointer;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.model.MapBasedActivityRepository;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;



import static org.assertj.core.api.Assertions.assertThat;

class GetGeoJsonUseCaseTest {

    @Test
    void asGeoJsonReturnsSanitizedTrackWithTrackProperties() {
        ZonedDateTime start = ZonedDateTime.parse("2024-05-01T10:15:30Z");
        Activity activity = activity(start, List.of(
                waypoint(0, 45.1, 5.1, 400, 310, 125, 0),
                new Waypoint(60, Maybe.none(), Maybe.none(), Maybe.some(320), Maybe.some(130), Maybe.none(),
                             Maybe.some(450), Maybe.some(100), 1),
                waypoint(120, 45.2, 5.2, null, null, null, 200)
                                                   ));
        MapBasedActivityRepository activityRepository = new MapBasedActivityRepository(Map.of(activity.id(), activity));
        GetGeoJsonUseCase testee = new GetGeoJsonUseCase(activityRepository);

        FeatureCollection result = (FeatureCollection) testee.asGeoJson(activity.id(), List.of());

        assertThat(result.getFeatures()).hasSize(1);

        Feature track = result.getFeatures().getFirst();
        assertThat(track.getGeometry()).isInstanceOf(LineString.class);

        LineString geometry = (LineString) track.getGeometry();
        assertThat(geometry.getCoordinates())
                .extracting(LngLatAlt::getLongitude, LngLatAlt::getLatitude, LngLatAlt::getAltitude)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(5.1, 45.1, 400.0),
                        org.assertj.core.groups.Tuple.tuple(5.2, 45.2, 0.0)
                                );
        assertThat(track.getProperties())
                .containsEntry("heartRates", List.of(125, 0))
                .containsEntry("powers", List.of(310, 0))
                .containsEntry("coordTimes", List.of(
                        start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        start.plusSeconds(120).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                                    ))
                .containsEntry("time", start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    @Test
    void asGeoJsonAddsClimbStartAndEndMarkers() {
        ZonedDateTime start = ZonedDateTime.parse("2024-05-01T10:15:30Z");
        Activity activity = activity(start, List.of(
                waypoint(0, 47.0, 5.0, 300, 300, 120, 0),
                waypoint(60, 45.1, 5.1, 450, 330, 120, 100),
                waypoint(120, 45.2, 5.2, 650, 360, 120, 300)
                                                   ));
        GetGeoJsonUseCase testee = new GetGeoJsonUseCase(new MapBasedActivityRepository(Map.of(activity.id(),
                                                                                               activity)));

        FeatureCollection result = (FeatureCollection) testee.asGeoJson(activity.id(), List.of(
                new ClimbPointer(100, 300, 250, 10, activity.bounds())
                                                                                              ));

        assertThat(result.getFeatures()).hasSize(3);

        Feature climbStart = result.getFeatures().get(1);
        assertThat(climbStart.getGeometry()).isInstanceOf(Point.class);
        assertThat(((Point) climbStart.getGeometry()).getCoordinates())
                .extracting(LngLatAlt::getLongitude, LngLatAlt::getLatitude, LngLatAlt::getAltitude)
                .containsExactly(5.1, 45.1, 450.0);
        assertThat(climbStart.getProperties())
                .containsEntry("name", "Climb start")
                .containsEntry("desc", "Ascent: 250, descent: 10, len: 200, alt: 450");

        Feature climbEnd = result.getFeatures().get(2);
        assertThat(climbEnd.getGeometry()).isInstanceOf(Point.class);
        assertThat(((Point) climbEnd.getGeometry()).getCoordinates())
                .extracting(LngLatAlt::getLongitude, LngLatAlt::getLatitude, LngLatAlt::getAltitude)
                .containsExactly(5.2, 45.2, 650.0);
        assertThat(climbEnd.getProperties())
                .containsEntry("name", "Climb end")
                .containsEntry("desc", "Alt: 650 ");
    }

    private static Activity activity(ZonedDateTime start, List<Waypoint> waypoints) {
        return Activity.builder()
                       .id(42L)
                       .title("Morning ride")
                       .start(start)
                       .waypoints(waypoints)
                       .build();
    }

    private static Waypoint waypoint(int secondsSinceStart,
                                     double lat,
                                     double lon,
                                     Integer altitude,
                                     Integer power,
                                     Integer heartRate,
                                     Integer distanceInMeters) {
        return new Waypoint(secondsSinceStart, Maybe.some(new LatLon(lat, lon)), Maybe.none(), power == null ?
                                                                                               Maybe.none() :
                                                                                               Maybe.some(power),
                            heartRate == null ? Maybe.none() : Maybe.some(heartRate), Maybe.none(), altitude == null
                                                                                                    ? Maybe.none() :
                                                                                                    Maybe.some(altitude), Maybe.some(distanceInMeters), 1);
    }
}
