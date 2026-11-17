package net.privactivity.fitgenerator;

public record GeoPoint(double lat, double lon) {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    public double distanceTo(GeoPoint other) {
        double dLat = Math.toRadians(other.lat - lat);
        double dLon = Math.toRadians(other.lon - lon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                   + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(other.lat))
                     * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
    }

    public GeoPoint interpolate(GeoPoint other, double fraction) {
        return new GeoPoint(lat + (other.lat - lat) * fraction, lon + (other.lon - lon) * fraction);
    }
}
