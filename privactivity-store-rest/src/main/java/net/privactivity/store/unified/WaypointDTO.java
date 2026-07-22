package net.privactivity.store.unified;

import net.privactivity.domain.LatLon;
import net.privactivity.domain.Waypoint;

public record WaypointDTO(
        int secondsSinceStart,
        LatLon latlon,
        int speedInMeterPerHour,
        int power,
        int heartRate,
        int cadence,
        int altitude,
        int distanceInMeter
) {
    public WaypointDTO(Waypoint waypoint) {
        this(
                waypoint.secondsSinceStart(),
                waypoint.latlon().orElse(new LatLon(0, 0)),
                waypoint.speedInMeterPerHour().orElse(0),
                waypoint.power().orElse(0),
                waypoint.heartRate().orElse(0),
                waypoint.cadence().orElse(0),
                waypoint.altitude().orElse(0),
                waypoint.distanceInMeter().orElse(0)
            );
    }
}