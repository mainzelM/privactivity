package net.privactivity.domain;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Activity(long id,
                       String title,
                       ZonedDateTime start,
                       List<Waypoint> waypoints,
                       Totals totals,
                       Averages averages,
                       Maxima maxima,
                       List<ClimbPointer> climbPointers,
                       Bounds bounds,
                       Summary summary) {
    public Activity {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(waypoints, "waypoints must not be null");
        Objects.requireNonNull(totals, "totals must not be null");
        Objects.requireNonNull(averages, "averages must not be null");
        Objects.requireNonNull(maxima, "maxima must not be null");
        Objects.requireNonNull(climbPointers, "climbPointers must not be null");
        Objects.requireNonNull(bounds, "bounds must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
    }

    public Activity(long id, String title, ZonedDateTime start, List<Waypoint> waypoints, Totals totals,
                    Averages averages, Maxima maxima, List<ClimbPointer> climbPointers, Bounds bounds) {
        this(id, title, start, waypoints, totals, averages, maxima, climbPointers, bounds, new Summary());
    }

    public Activity(long id, String title, ZonedDateTime start, List<Waypoint> waypoints, Duration movingTime) {
        this(id, title, start, waypoints, new Totals(movingTime), new Averages(), new Maxima(), new ArrayList<>(),
             Bounds.ofWaypoints(waypoints), new Summary());
    }

    public Activity withTitle(String newTitle) {
        Objects.requireNonNull(newTitle, "newTitle must not be null");
        return new Activity(id, newTitle, start, waypoints, totals, averages, maxima, climbPointers, bounds, summary);
    }

    public Activity withTotals(Totals newTotals) {
        Objects.requireNonNull(newTotals, "newTotals must not be null");
        return new Activity(id, title, start, waypoints, newTotals, averages, maxima, climbPointers, bounds, summary);
    }

    public Activity withMaxima(Maxima newMaxima) {
        Objects.requireNonNull(newMaxima, "newMaxima must not be null");
        return new Activity(id, title, start, waypoints, totals, averages, newMaxima, climbPointers, bounds, summary);
    }

    public Activity withAverages(Averages averages) {
        Objects.requireNonNull(averages, "averages must not be null");
        return new Activity(id, title, start, waypoints, totals, averages, maxima, climbPointers, bounds, summary);
    }

    public Activity withClimbs(List<ClimbPointer> newClimbPointers) {
        Objects.requireNonNull(newClimbPointers, "newClimbPointers must not be null");
        return new Activity(id, title, start, waypoints, totals, averages, maxima, newClimbPointers, bounds, summary);
    }

    public Activity withWaypoints(List<Waypoint> newWaypoints) {
        Objects.requireNonNull(newWaypoints, "newWaypoints must not be null");
        return new Activity(id, title, start, newWaypoints, totals, averages, maxima, climbPointers, bounds, summary);
    }

    public Activity withSummary(Summary newSummary) {
        Objects.requireNonNull(newSummary, "newSummary must not be null");
        return new Activity(id, title, start, waypoints, totals, averages, maxima, climbPointers, bounds, newSummary);
    }

    public void releaseWaypoints() {
        this.waypoints.clear();
    }

    public Activity withMovingTime(float movingDuration) {
        Duration movingTime = Duration.ofSeconds((int) movingDuration);
        return withTotals(new Totals(totals.distanceInMeters, totals.ascent,
                                     totals.descent, Maybe.some(movingTime),
                                     totals.calories));
    }

    public Activity withDistance(float distance) {
        return withTotals(new Totals(Maybe.some((int) distance), totals.ascent,
                                     totals.descent, totals.movingTime,
                                     totals.calories));
    }

    public Activity withAverageHeartRate(int averageHR) {
        return withAverages(new Averages(averages.speedInMetersPerHour(),
                                         averages.cadence(),
                                         Maybe.some(averageHR),
                                         averages.power()));
    }

    public Activity withMaximumHeartRate(int maxHR) {
        return withMaxima(new Maxima(maxima.speedInMetersPerHour(),
                                     Maybe.some(maxHR),
                                     maxima.power(),
                                     maxima.altitude()));
    }

    public Activity withAverageSpeed(int averageSpeed) {
        return withAverages(new Averages(Maybe.some(averageSpeed),
                                         averages.cadence(),
                                         averages.heartRate(),
                                         averages.power()));
    }

    public Activity withMaximumSpeed(int maxSpeed) {
        return withMaxima(new Maxima(Maybe.some(maxSpeed),
                                     maxima.heartRate(),
                                     maxima.power(),
                                     maxima.altitude()));
    }

    public Activity withAveragePower(int avgPower) {
        return withAverages(new Averages(averages.speedInMetersPerHour(),
                                         averages.cadence(),
                                         averages.heartRate(),
                                         Maybe.some(avgPower)));
    }

    public Activity withMaximumPower(int maxPower) {
        return withMaxima(new Maxima(maxima.speedInMetersPerHour(),
                                     maxima.heartRate(),
                                     Maybe.some(maxPower),
                                     maxima.altitude()));
    }

    public Activity withAscent(int ascent) {
        return withTotals(new Totals(totals.distanceInMeters, Maybe.some(ascent),
                                     totals.descent, totals.movingTime,
                                     totals.calories));
    }

    public Activity withDescent(int descent) {
        return withTotals(new Totals(totals.distanceInMeters, totals.ascent,
                                     Maybe.some(descent), totals.movingTime,
                                     totals.calories));
    }

    public Activity withMaxAltitutde(int maxAltitutde) {
        return withMaxima(new Maxima(maxima.speedInMetersPerHour, maxima.heartRate, maxima.power,
                                     Maybe.some(maxAltitutde)));
    }

    public Activity withStart(ZonedDateTime start) {
        return new Activity(id, title, start, waypoints, totals, averages, maxima, climbPointers, bounds, summary);
    }

    public record Totals(
            Maybe<Integer> distanceInMeters,
            Maybe<Integer> ascent,
            Maybe<Integer> descent,
            Maybe<Duration> movingTime,
            Maybe<Integer> calories) {
        public Totals(Duration movingTime) {
            this(Maybe.none(), Maybe.none(), Maybe.none(), Maybe.some(movingTime), Maybe.none());
        }
    }

    public record Averages(
            Maybe<Integer> speedInMetersPerHour,
            Maybe<Integer> cadence,
            Maybe<Integer> heartRate,
            Maybe<Integer> power) {
        public Averages() {
            this(Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none());
        }
    }

    public record Maxima(
            Maybe<Integer> speedInMetersPerHour,
            Maybe<Integer> heartRate,
            Maybe<Integer> power,
            Maybe<Integer> altitude) {
        public Maxima() {
            this(Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none());
        }
    }

    public record Summary(Maybe<Float> trainingStressScore,
                          Maybe<Float> totalTrainingEffect,
                          Maybe<Float> intensityFactor,
                          Maybe<Byte> avgLeftPco,
                          Maybe<Byte> avgRightPco) {
        public Summary() {
            this(Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none(), Maybe.none());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long id;
        private String title;
        private ZonedDateTime start;
        private List<Waypoint> waypoints = new ArrayList<>();
        private Totals totals;
        private Averages averages = new Averages();
        private Maxima maxima = new Maxima();
        private List<ClimbPointer> climbPointers = new ArrayList<>();
        private Bounds bounds;
        private Summary summary = new Summary();

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder start(ZonedDateTime start) {
            this.start = start;
            return this;
        }

        public Builder waypoints(List<Waypoint> waypoints) {
            this.waypoints = waypoints;
            return this;
        }

        public Builder totals(Totals totals) {
            this.totals = totals;
            return this;
        }

        public Builder averages(Averages averages) {
            this.averages = averages;
            return this;
        }

        public Builder maxima(Maxima maxima) {
            this.maxima = maxima;
            return this;
        }

        public Builder climbPointers(List<ClimbPointer> climbPointers) {
            this.climbPointers = climbPointers;
            return this;
        }

        public Builder bounds(Bounds bounds) {
            this.bounds = bounds;
            return this;
        }

        public Builder summary(Summary summary) {
            this.summary = summary;
            return this;
        }

        public Activity build() {
            if (totals == null) {
                totals = new Totals(Duration.ZERO);
            }
            if (bounds == null && waypoints != null) {
                bounds = Bounds.ofWaypoints(waypoints);
            }
            if (summary == null) {
                summary = new Summary();
            }
            return new Activity(id, title, start, waypoints, totals, averages, maxima, climbPointers, bounds, summary);
        }
    }
}
