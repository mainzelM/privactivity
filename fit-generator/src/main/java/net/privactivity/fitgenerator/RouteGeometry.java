package net.privactivity.fitgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RouteGeometry {

    private final List<GeoPoint> polyline;
    private final double[] cumulativeMeters;

    public RouteGeometry(List<GeoPoint> polyline) {
        if (polyline.size() < 2) {
            throw new GenerationException("Route needs at least two points");
        }
        this.polyline = List.copyOf(polyline);
        this.cumulativeMeters = new double[polyline.size()];
        for (int i = 1; i < polyline.size(); i++) {
            cumulativeMeters[i] = cumulativeMeters[i - 1] + polyline.get(i - 1).distanceTo(polyline.get(i));
        }
    }

    public double lengthMeters() {
        return cumulativeMeters[cumulativeMeters.length - 1];
    }

    public GeoPoint positionAt(double distanceMeters) {
        double d = Math.clamp(distanceMeters, 0, lengthMeters());
        int idx = Arrays.binarySearch(cumulativeMeters, d);
        if (idx >= 0) {
            return polyline.get(idx);
        }
        int upper = -idx - 1;
        double segmentLength = cumulativeMeters[upper] - cumulativeMeters[upper - 1];
        double fraction = segmentLength == 0 ? 0 : (d - cumulativeMeters[upper - 1]) / segmentLength;
        return polyline.get(upper - 1).interpolate(polyline.get(upper), fraction);
    }

    /**
     * @return points at 0, spacing, 2*spacing, ... and the route end
     */
    public List<GeoPoint> samplePoints(double spacingMeters) {
        List<GeoPoint> samples = new ArrayList<>();
        for (double d = 0; d < lengthMeters(); d += spacingMeters) {
            samples.add(positionAt(d));
        }
        samples.add(positionAt(lengthMeters()));
        return samples;
    }
}
