package net.privactivity.fitgenerator;

import java.util.List;

public record SessionSummary(double durationSeconds,
                             double distanceMeters,
                             int ascentMeters,
                             int descentMeters,
                             int calories,
                             double avgSpeedMetersPerSecond,
                             double maxSpeedMetersPerSecond,
                             int avgHeartRate,
                             int maxHeartRate,
                             int avgCadence,
                             int maxCadence,
                             int avgPower,
                             int maxPower,
                             int normalizedPower,
                             double intensityFactor,
                             double trainingStressScore,
                             double totalTrainingEffect,
                             double avgAltitudeMeters,
                             double maxAltitudeMeters,
                             int avgTemperature,
                             int maxTemperature) {

    /**
     * Aggregates the way a head unit does: moving time equals timer time as the ride has no pauses,
     * cadence average ignores coasting.
     */
    public static SessionSummary of(List<RideRecord> records, RiderProfile rider) {
        RideRecord last = records.getLast();
        double duration = last.secondsSinceStart();
        double ascent = 0;
        double descent = 0;
        double powerSum = 0;
        double hrSum = 0;
        double altitudeSum = 0;
        double temperatureSum = 0;
        double cadenceSum = 0;
        int cadenceCount = 0;
        SessionSummaryMaxima max = new SessionSummaryMaxima();
        for (int i = 0; i < records.size(); i++) {
            RideRecord r = records.get(i);
            if (i > 0) {
                double delta = r.altitudeMeters() - records.get(i - 1).altitudeMeters();
                ascent += Math.max(0, delta);
                descent += Math.max(0, -delta);
            }
            powerSum += r.power();
            hrSum += r.heartRate();
            altitudeSum += r.altitudeMeters();
            temperatureSum += r.temperatureCelsius();
            if (r.cadence() > 0) {
                cadenceSum += r.cadence();
                cadenceCount++;
            }
            max.accept(r);
        }
        int n = records.size();
        double normalizedPower = normalizedPower(records);
        double intensityFactor = normalizedPower / rider.ftpWatts();
        double tss = duration * normalizedPower * intensityFactor / (rider.ftpWatts() * 3600) * 100;
        double trainingEffect = Math.min(5.0, Math.round(10 * (1 + 1.1 * Math.log(1 + tss / 25))) / 10.0);
        double workKilojoules = powerSum / 1000;
        return new SessionSummary(duration,
                                  last.distanceMeters(),
                                  (int) Math.round(ascent),
                                  (int) Math.round(descent),
                                  (int) Math.round(workKilojoules / (0.24 * 4.184)),
                                  last.distanceMeters() / duration,
                                  max.speed,
                                  (int) Math.round(hrSum / n),
                                  max.heartRate,
                                  cadenceCount == 0 ? 0 : (int) Math.round(cadenceSum / cadenceCount),
                                  max.cadence,
                                  (int) Math.round(powerSum / n),
                                  max.power,
                                  (int) Math.round(normalizedPower),
                                  Math.round(intensityFactor * 1000) / 1000.0,
                                  Math.round(tss * 10) / 10.0,
                                  trainingEffect,
                                  altitudeSum / n,
                                  max.altitude,
                                  (int) Math.round(temperatureSum / n),
                                  max.temperature);
    }

    private static double normalizedPower(List<RideRecord> records) {
        int window = 30;
        if (records.size() < window) {
            return records.stream().mapToInt(RideRecord::power).average().orElse(0);
        }
        double windowSum = 0;
        double fourthPowerSum = 0;
        int count = 0;
        for (int i = 0; i < records.size(); i++) {
            windowSum += records.get(i).power();
            if (i >= window) {
                windowSum -= records.get(i - window).power();
            }
            if (i >= window - 1) {
                fourthPowerSum += Math.pow(windowSum / window, 4);
                count++;
            }
        }
        return Math.pow(fourthPowerSum / count, 0.25);
    }

    private static class SessionSummaryMaxima {
        double speed;
        int heartRate;
        int cadence;
        int power;
        double altitude = Double.NEGATIVE_INFINITY;
        int temperature = Integer.MIN_VALUE;

        void accept(RideRecord r) {
            speed = Math.max(speed, r.speedMetersPerSecond());
            heartRate = Math.max(heartRate, r.heartRate());
            cadence = Math.max(cadence, r.cadence());
            power = Math.max(power, r.power());
            altitude = Math.max(altitude, r.altitudeMeters());
            temperature = Math.max(temperature, r.temperatureCelsius());
        }
    }
}
