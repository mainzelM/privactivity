package net.privactivity.store.usecase.townfinder.findmaintowns;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Bounds;
import net.privactivity.domain.HaversineDistance;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.usecase.townfinder.adapter.TownFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FindActivityMainTownsUseCase {
    private final TownFinder townFinder;

    public FindActivityMainTownsUseCase(TownFinder townFinder) {
        this.townFinder = townFinder;
    }

    public List<String> act(Activity activity) {
        List<Waypoint> allWaypoints = activity.waypoints();
        if (allWaypoints.isEmpty()) {
            return List.of();
        }
        List<Waypoint> reduced = reduceWaypoints(allWaypoints);


        return townFinder.findNearbyTowns(reduced);
    }

    private List<Waypoint> reduceWaypoints(List<Waypoint> allWaypoints) {
        if (allWaypoints.isEmpty()) {
            return List.of();
        }
        List<Waypoint> reduced = new ArrayList<>();
        Waypoint last = allWaypoints.getFirst();
        reduced.add(last);
        for (Waypoint w : allWaypoints.subList(1, allWaypoints.size())) {
            if (HaversineDistance.distanceInMeter(last, w).orElse(0d) > 500) {
                reduced.add(w);
                last = w;
            }
        }
        return reduced;
    }

    public List<String> findMainTownsOnBounds(Activity activity) {
        Optional<BoundsWaypoints> cornersOpt = findBoundsWaypoints(activity);

        if (cornersOpt.isEmpty()) {
            return List.of();
        }

        BoundsWaypoints corners = cornersOpt.get();
        List<Waypoint> cornerWaypoints = List.of(
                corners.northmost(),
                corners.southmost(),
                corners.eastmost(),
                corners.westmost());

        return townFinder.findNearbyTowns(cornerWaypoints);
    }

    public List<String> findMainTownsOnCorners(Activity activity) {
        Bounds bounds = activity.bounds();

        LatLon northEast = new LatLon(bounds.north(), bounds.east());
        LatLon southEast = new LatLon(bounds.south(), bounds.east());
        LatLon southWest = new LatLon(bounds.south(), bounds.west());
        LatLon northWest = new LatLon(bounds.north(), bounds.west());


        return townFinder.findNearbyTownsFromLatLons(List.of(northEast, southEast, southWest, northWest));
    }

    private Waypoint findFirstWaypoint(List<Waypoint> waypoints) {
        return waypoints.stream()
                        .filter(wp -> wp.latlon().isPresent())
                        .findFirst()
                        .orElse(null);
    }

    private Optional<BoundsWaypoints> findBoundsWaypoints(Activity activity) {
        List<Waypoint> waypoints = activity.waypoints();

        Waypoint firstWaypoint = findFirstWaypoint(waypoints);
        if (firstWaypoint == null) {
            return Optional.empty();
        }

        Waypoint northmost = firstWaypoint;
        Waypoint southmost = firstWaypoint;
        Waypoint eastmost = firstWaypoint;
        Waypoint westmost = firstWaypoint;

        for (Waypoint waypoint : waypoints) {
            if (waypoint.latlon().isPresent()) {
                double lat = waypoint.latlon().orThrow().lat();
                if (lat > northmost.latlon().orThrow().lat()) {
                    northmost = waypoint;
                } else if (lat < southmost.latlon().orThrow().lat()) {
                    southmost = waypoint;
                }

                double lon = waypoint.latlon().orThrow().lon();
                if (lon > eastmost.latlon().orThrow().lon()) {
                    eastmost = waypoint;
                } else if (lon < westmost.latlon().orThrow().lon()) {
                    westmost = waypoint;
                }
            }
        }

        return Optional.of(new BoundsWaypoints(northmost, southmost, eastmost, westmost));
    }

    public record BoundsWaypoints(Waypoint northmost, Waypoint southmost, Waypoint eastmost, Waypoint westmost) {
    }
}
