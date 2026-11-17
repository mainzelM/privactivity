package net.privactivity.fitgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simulates a steady endurance ride without stops. Speed follows from a physical power model
 * (gravity, rolling and air resistance), heart rate lags behind power.
 */
public class RideSimulator {

    private static final double AIR_DENSITY = 1.2;
    private static final double GRAVITY = 9.81;
    private static final double DRIVETRAIN_EFFICIENCY = 0.97;
    private static final double SPEED_INERTIA = 0.15;
    private static final double COASTING_SPEED = 12.0;
    private static final double MAX_SPEED = 17.0;
    private static final double MIN_SPEED = 1.5;

    private final RiderProfile rider;
    private final Random random;

    public RideSimulator(RiderProfile rider, long seed) {
        this.rider = rider;
        this.random = new Random(seed);
    }

    public List<RideRecord> simulate(RouteGeometry route, ElevationProfile elevation) {
        List<RideRecord> records = new ArrayList<>();
        double length = route.lengthMeters();
        double distance = 0;
        double speed = 0;
        double effort = 0;
        double averagedPower = rider.ftpWatts() * 0.5;
        double heartRate = rider.restingHeartRate() + 25;
        double baseTemperature = 12 + random.nextInt(10);
        double startAltitude = elevation.elevationAt(0);

        for (int second = 0; ; second++) {
            double grade = elevation.gradeAt(distance);
            effort += -effort * 0.005 + random.nextGaussian() * 0.012;
            effort = Math.clamp(effort, -0.25, 0.3);
            double targetPower = rider.ftpWatts() * 0.68 * (1 + effort) * (grade > 0.03 ? 1.15 : 1.0);
            targetPower = Math.min(targetPower, rider.ftpWatts() * 1.1);

            double steadySpeed = speedForPower(targetPower, grade);
            int power = (int) Math.round(targetPower * (1 + random.nextGaussian() * 0.07));
            if (grade < -0.01 && steadySpeed > COASTING_SPEED) {
                power = 0;
                steadySpeed = Math.min(MAX_SPEED, speedForPower(0, grade));
            }
            power = Math.max(0, power);
            speed += (Math.max(MIN_SPEED, steadySpeed) - speed) * SPEED_INERTIA;
            if (second == 0) {
                speed = 0;
            }

            averagedPower += (power - averagedPower) / 30;
            double hrTarget = rider.restingHeartRate()
                              + (rider.maxHeartRate() - rider.restingHeartRate())
                                * (0.35 + 0.5 * averagedPower / rider.ftpWatts())
                              + second / 3600.0 * 3;
            heartRate += (hrTarget - heartRate) / 45;
            int hr = (int) Math.round(Math.min(rider.maxHeartRate() - 1, heartRate + random.nextGaussian()));

            int cadence = power == 0 ? 0 : (int) Math.round(Math.clamp(
                    (grade > 0.04 ? 76 : 89) + random.nextGaussian() * 3, 55, 115));
            double altitude = Math.round(elevation.elevationAt(distance) * 5) / 5.0;
            int temperature = (int) Math.round(baseTemperature - 0.0065 * (altitude - startAltitude));

            records.add(new RideRecord(second, route.positionAt(distance), distance, speed, altitude, hr,
                                       cadence, power, temperature, grade * 100));
            if (distance >= length) {
                return records;
            }
            distance = Math.min(length, distance + speed);
        }
    }

    /**
     * Solves wheel power = v * (m*g*(crr + grade) + 0.5*rho*CdA*v^2) for v by bisection.
     */
    double speedForPower(double powerWatts, double grade) {
        double wheelPower = powerWatts * DRIVETRAIN_EFFICIENCY;
        double slopeForce = rider.totalKg() * GRAVITY * (rider.rollingResistance() + grade);
        double low = 0;
        double high = 40;
        for (int i = 0; i < 40; i++) {
            double v = (low + high) / 2;
            double required = v * (slopeForce + 0.5 * AIR_DENSITY * rider.dragAreaM2() * v * v);
            if (required > wheelPower) {
                high = v;
            } else {
                low = v;
            }
        }
        return low;
    }
}
