package net.privactivity.domain;

import java.util.List;
import java.util.stream.Collectors;

public record Bounds(LatLon southernmostLatLon, LatLon northernmostLatLon, LatLon westernmostLatLon,
                     LatLon easternmostLatLon) {

    public static Bounds of(double southernmostLat, double northernmostLat, double westernmostLon,
                            double easternmostLon) {
        return new Bounds(new LatLon(southernmostLat, 0),
                          new LatLon(northernmostLat, 0),
                          new LatLon(0, westernmostLon),
                          new LatLon(0, easternmostLon));
    }

    public boolean intersects(Bounds other) {
        return southernmostLatLon.lat() <= other.northernmostLatLon.lat() && northernmostLatLon.lat() >= other.southernmostLatLon.lat() &&
               westernmostLatLon.lon() <= other.easternmostLatLon.lon() && easternmostLatLon.lon() >= other.westernmostLatLon.lon();
    }

    public boolean contains(LatLon latLon) {
        return latLon.lat() >= south() && latLon.lat() <= north() &&
               latLon.lon() >= west() && latLon.lon() <= east();
    }

    public static Bounds ofWaypoints(List<Waypoint> waypoints) {
        return ofLatLons(waypoints.stream()
                                  .map(Waypoint::latlon)
                                  .filter(Some.class::isInstance)
                                  .map(Some.class::cast)
                                  .map(Some::value)
                                  .map(LatLon.class::cast)
                                  .collect(Collectors.toList()));
    }

    public static Bounds ofLatLons(List<LatLon> latLons) {
        LatLon southernmostWp = new LatLon(90d, -180d);
        LatLon northernmostWP = new LatLon(-90d, 180d);
        LatLon westernmostWP = new LatLon(90d, 180d);
        LatLon easternmostWP = new LatLon(-90d, -180d);

        for (LatLon wp : latLons) {
            double lon = wp.lon();
            if (lon > easternmostWP.lon()) {
                easternmostWP = wp;
            }
            if (lon < westernmostWP.lon()) {
                westernmostWP = wp;
            }
            double lat = wp.lat();
            if (lat < southernmostWp.lat()) {
                southernmostWp = wp;
            }
            if (lat > northernmostWP.lat()) {
                northernmostWP = wp;
            }
        }
        return new Bounds(southernmostWp, northernmostWP, westernmostWP, easternmostWP);
    }

    public double south() {
        return southernmostLatLon.lat();
    }

    public double north() {
        return northernmostLatLon.lat();
    }

    public double west() {
        return westernmostLatLon.lon();
    }

    public double east() {
        return easternmostLatLon.lon();
    }
}
