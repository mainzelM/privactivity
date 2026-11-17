package net.privactivity.fitgenerator;

import java.util.List;

/**
 * Smoothed elevation along a route, built from samples taken at a fixed spacing
 * (the last sample is the route end).
 */
public class ElevationProfile {

    private static final int SMOOTHING_RADIUS = 1;
    private static final double GRADE_WINDOW_METERS = 100;
    private static final double MAX_GRADE = 0.16;

    private final double spacingMeters;
    private final double lengthMeters;
    private final double[] elevations;

    public ElevationProfile(double spacingMeters, double lengthMeters, List<Double> rawElevations) {
        this.spacingMeters = spacingMeters;
        this.lengthMeters = lengthMeters;
        this.elevations = smooth(rawElevations);
    }

    public double elevationAt(double distanceMeters) {
        double position = Math.clamp(distanceMeters, 0, lengthMeters) / spacingMeters;
        int lower = (int) Math.floor(position);
        if (lower >= elevations.length - 1) {
            return elevations[elevations.length - 1];
        }
        double fraction = position - lower;
        return elevations[lower] + (elevations[lower + 1] - elevations[lower]) * fraction;
    }

    /**
     * @return grade as fraction (0.05 = 5 %), averaged over a window around the position
     */
    public double gradeAt(double distanceMeters) {
        double from = Math.max(0, distanceMeters - GRADE_WINDOW_METERS / 2);
        double to = Math.min(lengthMeters, distanceMeters + GRADE_WINDOW_METERS / 2);
        if (to - from < 1) {
            return 0;
        }
        double grade = (elevationAt(to) - elevationAt(from)) / (to - from);
        return Math.clamp(grade, -MAX_GRADE, MAX_GRADE);
    }

    private double[] smooth(List<Double> raw) {
        double[] result = new double[raw.size()];
        for (int i = 0; i < raw.size(); i++) {
            int from = Math.max(0, i - SMOOTHING_RADIUS);
            int to = Math.min(raw.size() - 1, i + SMOOTHING_RADIUS);
            double sum = 0;
            for (int j = from; j <= to; j++) {
                sum += raw.get(j);
            }
            result[i] = sum / (to - from + 1);
        }
        return result;
    }
}
