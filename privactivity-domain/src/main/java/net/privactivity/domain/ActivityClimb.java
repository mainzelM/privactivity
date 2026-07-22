package net.privactivity.domain;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record ActivityClimb(long activityId,
                            ZonedDateTime startTime,
                            int startAltitude,
                            int endAltitude,
                            LatLon startLatLon,
                            LatLon endLatLon,
                            int lengthInMeters,
                            Duration duration,
                            int ascent,
                            int descent,
                            Maybe<Integer> avgPower,
                            Maybe<Integer> maxPower,
                            Maybe<Integer> avgHeartRate,
                            Maybe<Integer> maxHeartRate) {
    public static ActivityClimb fromActivity(Activity a, ClimbPointer ac) {
        Waypoint startWp = findWpAt(a, ac.startMeters());
        Waypoint endWp = findWpAt(a, ac.endMeters());
        List<Waypoint> climbWps = findWpBetween(a, ac.startMeters(), ac.endMeters());
        return new ActivityClimb(a.id(),
                                 a.start(),
                                 startWp.altitude().orThrow(),
                                 endWp.altitude().orThrow(),
                                 startWp.latlon().orThrow(),
                                 endWp.latlon().orThrow(),
                                 endWp.distanceInMeter().orThrow() - startWp.distanceInMeter().orThrow(),
                                 Duration.of(endWp.secondsSinceStart() - startWp.secondsSinceStart(),
                                             ChronoUnit.SECONDS),
                                 ac.ascent(),
                                 ac.descent(),
                                 calcAvgPower(climbWps),
                                 calcMaxPower(climbWps),
                                 calcAvgHeartRate(climbWps),
                                 calcMaxHeartRate(climbWps));
    }

    private static Maybe<Integer> calcAvgPower(List<Waypoint> climbWps) {
        return calcAvg(climbWps, Waypoint::power);
    }

    private static Maybe<Integer> calcAvgHeartRate(List<Waypoint> climbWps) {
        return calcAvg(climbWps, Waypoint::heartRate);
    }

    private static Maybe<Integer> calcAvg(List<Waypoint> climbWps, Function<Waypoint, Maybe<Integer>> extract) {
        return Maybe.fromOptional(climbWps.stream()
                                          .map(extract)
                                          .filter(Some.class::isInstance)
                                          .mapToInt(Maybe::orThrow)
                                          .average())
                    .map(Math::round)
                    .map(Math::toIntExact);
    }

    private static Maybe<Integer> calcMaxPower(List<Waypoint> climbWps) {
        return calcMax(climbWps, Waypoint::power);
    }

    private static Maybe<Integer> calcMaxHeartRate(List<Waypoint> climbWps) {
        return calcMax(climbWps, Waypoint::heartRate);
    }

    private static Maybe<Integer> calcMax(List<Waypoint> climbWps, Function<Waypoint, Maybe<Integer>> extract) {
        return Maybe.fromOptional(climbWps.stream()
                                          .map(extract)
                                          .filter(Some.class::isInstance)
                                          .mapToInt(Maybe::orThrow)
                                          .max())
                    .map(Math::round)
                    .map(Integer.class::cast);
    }

    private static List<Waypoint> findWpBetween(Activity a, int startMeters, int endMeters) {
        return a.waypoints().stream()
                .filter(wp -> wp.distanceInMeter().orThrow() >= startMeters)
                .filter(wp -> wp.distanceInMeter().orThrow() <= endMeters)
                .collect(Collectors.toList());
    }

    private static Waypoint findWpAt(Activity a, int meters) {
        return a.waypoints().stream()
                .filter(wp -> wp.distanceInMeter().orThrow().equals(meters))
                .findFirst()
                .orElseThrow();
    }
}
