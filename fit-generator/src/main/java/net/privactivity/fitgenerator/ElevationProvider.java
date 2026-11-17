package net.privactivity.fitgenerator;

import java.util.List;

public interface ElevationProvider {

    /**
     * @return elevation in meters for each of the given points, in the same order
     */
    List<Double> elevations(List<GeoPoint> points);
}
