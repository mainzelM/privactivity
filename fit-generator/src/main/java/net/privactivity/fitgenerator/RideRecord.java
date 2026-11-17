package net.privactivity.fitgenerator;

/**
 * One 1 Hz sample of the simulated ride.
 */
public record RideRecord(int secondsSinceStart,
                         GeoPoint position,
                         double distanceMeters,
                         double speedMetersPerSecond,
                         double altitudeMeters,
                         int heartRate,
                         int cadence,
                         int power,
                         int temperatureCelsius,
                         double gradePercent) {
}
