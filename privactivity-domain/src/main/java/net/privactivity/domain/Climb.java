package net.privactivity.domain;

import java.util.List;
import java.util.stream.Collectors;

public record Climb(List<LatLon> latLons, Bounds bounds) {
    public Climb(List<LatLon> latLons) {
        this(latLons, Bounds.ofLatLons(latLons));
    }

    public static List<Climb> of(Activity activity) {
        return activity.climbPointers().stream()
                       .map(ac -> ofActivityClimb(activity, ac))
                       .collect(Collectors.toList());
    }

    public static Climb ofActivityClimb(Activity activity, ClimbPointer ac) {
        int start = ac.startMeters();
        int end = ac.endMeters();
        List<LatLon> latLons = activity.waypoints().stream()
                                       .filter(wp -> wp.distanceInMeter().orElse(-1) >= start)
                                       .filter(wp -> wp.distanceInMeter().orElse(Integer.MAX_VALUE) <= end)
                                       .filter(wp -> wp.latlon() instanceof Some<LatLon>)
                                       .map(wp -> wp.latlon().orThrow())
                                       .collect(Collectors.toList());
        return new Climb(latLons);
    }
}
