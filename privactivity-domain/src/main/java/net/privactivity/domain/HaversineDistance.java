package net.privactivity.domain;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

public class HaversineDistance {
    private final static double AVERAGE_RADIUS_OF_EARTH_METERS = 6378137d;

    public static double distanceInMeter(double startLat, double startLon,
                                         double endLat, double endLon) {
        double p = Math.PI / 180;
        double a =
                0.5 - cos((endLat - startLat) * p) / 2 + cos(startLat * p) * cos(endLat * p) * (1 - cos((endLon - startLon) * p)) / 2;
        return 2 * AVERAGE_RADIUS_OF_EARTH_METERS * asin(sqrt(a));
    }

    public static Maybe<Double> distanceInMeter(Waypoint start, Waypoint end) {
        if (start.latlon().equals(Maybe.none()) || end.latlon().equals(Maybe.none())) {
            return Maybe.none();
        } else {
            return Maybe.some(distanceInMeter(start.latlon().orThrow(), end.latlon().orThrow()));
        }
    }

    public static double distanceInMeter(LatLon start, LatLon end) {
        return distanceInMeter(start.lat(), start.lon(),
                               end.lat(), end.lon());
    }
}
