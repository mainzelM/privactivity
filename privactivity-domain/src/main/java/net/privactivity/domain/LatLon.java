package net.privactivity.domain;

public record LatLon(double lat, double lon) {
    private static final double SEMI_CIRCLE_TO_GRADE_FACTOR = (180d / (2L << 30));

    public LatLon(int latSemiCircle, int lonSemiCircle) {
        this(semiCircleToGrade(latSemiCircle), semiCircleToGrade(lonSemiCircle));
    }

    private static double semiCircleToGrade(long semiCircle) {
        return semiCircle * SEMI_CIRCLE_TO_GRADE_FACTOR;
    }
}
