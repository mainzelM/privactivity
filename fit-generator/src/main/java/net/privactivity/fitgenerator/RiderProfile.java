package net.privactivity.fitgenerator;

/**
 * Default rider/bike, similar to a trained amateur on a road bike with a power meter.
 */
public record RiderProfile(int ftpWatts,
                           double riderKg,
                           double bikeKg,
                           int maxHeartRate,
                           int restingHeartRate,
                           double dragAreaM2,
                           double rollingResistance,
                           int leftPcoMm,
                           int rightPcoMm) {

    public RiderProfile() {
        this(250, 75, 8.5, 185, 52, 0.32, 0.004, 1, 2);
    }

    public double totalKg() {
        return riderKg + bikeKg;
    }
}
