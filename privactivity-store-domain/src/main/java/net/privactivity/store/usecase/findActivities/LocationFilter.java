package net.privactivity.store.usecase.findActivities;

import net.privactivity.domain.Activity;
import net.privactivity.domain.HaversineDistance;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.model.Filter;

public record LocationFilter(double lat, double lon, int radiusMeters) implements Filter {
    @Override
    public boolean apply(Activity activity) {
        return activity.waypoints().stream()
                       .map(Waypoint::latlon)
                       .filter(Maybe::isPresent)
                       .map(Maybe::orThrow)
                       .anyMatch(ll -> HaversineDistance.distanceInMeter(ll.lat(), ll.lon(), lat, lon) <= radiusMeters);
    }

    public boolean preCheck(Activity activity) {
        if (activity.bounds() == null)
            return false;
        // conservative check: expand bounds by radius
        double bufferDegrees = (double) radiusMeters / 111000.0 * 1.5;
        double searchSouth = lat - bufferDegrees;
        double searchNorth = lat + bufferDegrees;
        double searchWest = lon - bufferDegrees;
        double searchEast = lon + bufferDegrees;

        return activity.bounds().north() >= searchSouth &&
               activity.bounds().south() <= searchNorth &&
               activity.bounds().east() >= searchWest &&
               activity.bounds().west() <= searchEast;
    }
}
