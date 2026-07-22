package net.privactivity.domain;

public record Waypoint(int secondsSinceStart,
                       Maybe<LatLon> latlon,
                       Maybe<Integer> speedInMeterPerHour,
                       Maybe<Integer> power,
                       Maybe<Integer> heartRate,
                       Maybe<Integer> cadence,
                       Maybe<Integer> altitude,
                       Maybe<Integer> distanceInMeter,
                       int lap) {

    public Waypoint {
        if (power.orElse(0) > 1500) {
            power = Maybe.none();
        }
    }
}
