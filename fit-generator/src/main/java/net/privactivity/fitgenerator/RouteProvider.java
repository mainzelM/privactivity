package net.privactivity.fitgenerator;

import java.util.List;

public interface RouteProvider {

    /**
     * @return the cycling route polyline from start to end
     */
    List<GeoPoint> route(GeoPoint start, GeoPoint end);
}
