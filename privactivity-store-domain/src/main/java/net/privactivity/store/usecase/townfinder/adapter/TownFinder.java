package net.privactivity.store.usecase.townfinder.adapter;

import net.privactivity.domain.LatLon;
import net.privactivity.domain.Waypoint;
import java.util.List;

public interface TownFinder {
    List<String> findNearbyTowns(List<Waypoint> waypoints);

    List<String> findNearbyTownsFromLatLons(List<LatLon> latLons);
}
