package net.privactivity.store.usecase.crossedpasses;

import net.privactivity.domain.HaversineDistance;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Waypoint;

public record MountainPass(String name, String country, LatLon summitLatLon) {
    public boolean waypointCloseToPassSummit(Waypoint waypoint) {
        return HaversineDistance.distanceInMeter(summitLatLon(), waypoint.latlon().orThrow()) < 50;
    }
}
