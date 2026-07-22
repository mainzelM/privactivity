package net.privactivity.store.usecase.climbdetector;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Bounds;
import net.privactivity.domain.ClimbPointer;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.adapter.ActivityRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClimbDetectorUseCase {

    private final ActivityRepository activityRepository;
    private final int minClimbAscent;
    private final int maxDescentPercentage;

    public ClimbDetectorUseCase(ActivityRepository activityRepository, int minClimbAscent, int maxDescentPercentage) {
        this.activityRepository = activityRepository;
        this.minClimbAscent = minClimbAscent;
        this.maxDescentPercentage = maxDescentPercentage;
    }

    public List<ClimbPointer> detect(long activityId) {
        Activity activity = activityRepository.getActivityById(activityId, true);
        try {
            return detectWaypoints(activity.waypoints());
        } finally {
            activity.releaseWaypoints();
        }
    }

    private List<ClimbPointer> detectWaypoints(List<Waypoint> waypoints) {
        if (waypoints.isEmpty())
            return List.of();

        List<FinishedClimb> detected = detectAll(waypoints);
        List<FinishedClimb> cleaned = new ArrayList<>(cleanup(detected));
        cleaned.sort(Comparator.comparingInt(fc -> fc.wps().getFirst().distanceInMeter().orElse(0)));
        return cleaned.stream().map(this::toClimbPointer).toList();
    }

    private List<FinishedClimb> detectAll(List<Waypoint> waypoints) {
        List<OngoingClimb> ongoing = new ArrayList<>();
        List<FinishedClimb> finished = new ArrayList<>();

        Waypoint previous = waypoints.getFirst();
        for (int i = 1; i < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i);
            List<OngoingClimb> nextOngoing = new ArrayList<>();

            OngoingClimb newClimb = maybeStartClimb(previous, wp);
            if (newClimb != null) {
                nextOngoing.add(newClimb);
            }

            for (OngoingClimb oc : ongoing) {
                switch (addWaypoint(wp, oc)) {
                    case OngoingClimb updated -> nextOngoing.add(updated);
                    case FinishedClimb fc -> finished.add(fc);
                    case AbandonedClimb ignored -> { /* discard */ }
                }
            }

            ongoing = nextOngoing;
            previous = wp;
        }

        for (OngoingClimb oc : ongoing) {
            if (oc.ascent() > minClimbAscent) {
                finished.add(removeTail(new FinishedClimb(oc.wps(), oc.ascent(), oc.descent())));
            }
        }

        return finished;
    }

    private OngoingClimb maybeStartClimb(Waypoint last, Waypoint wp) {
        int altLast = last.altitude().orElse(0);
        int altWp = wp.altitude().orElse(0);
        if (altWp > altLast) {
            List<Waypoint> wps = new ArrayList<>();
            wps.add(last);
            wps.add(wp);
            return new OngoingClimb(wps, altWp - altLast, 0);
        }
        return null;
    }

    private ClimbState addWaypoint(Waypoint newWp, OngoingClimb climb) {
        Waypoint lastWp = climb.wps().getLast();
        int diff = newWp.altitude().orElse(0) - lastWp.altitude().orElse(0);

        if (diff >= 0) {
            List<Waypoint> newWps = new ArrayList<>(climb.wps());
            newWps.add(newWp);
            return new OngoingClimb(newWps, climb.ascent() + diff, climb.descent());
        } else if (climb.ascent() * (maxDescentPercentage / 100.0) > (climb.descent() - diff)) {
            // diff is negative, so -diff adds to the accumulated descent
            List<Waypoint> newWps = new ArrayList<>(climb.wps());
            newWps.add(newWp);
            return new OngoingClimb(newWps, climb.ascent(), climb.descent() - diff);
        } else if (climb.ascent() > minClimbAscent) {
            return removeTail(new FinishedClimb(climb.wps(), climb.ascent(), climb.descent()));
        } else {
            return AbandonedClimb.INSTANCE;
        }
    }

    private static FinishedClimb removeTail(FinishedClimb fc) {
        int maxAlt = fc.wps().stream()
                       .mapToInt(wp -> wp.altitude().orElse(0))
                       .max()
                       .orElse(0);

        // Scan from the end of the climb (reversed) to find the last occurrence of
        // the peak that is immediately followed (going backwards) by a lower point.
        // Everything before that peak in the original list is retained.
        List<Waypoint> reversed = new ArrayList<>(fc.wps());
        Collections.reverse(reversed);

        int cutIndex = 0;
        for (int i = 0; i < reversed.size() - 1; i++) {
            if (reversed.get(i).altitude().orElse(0) == maxAlt
                && reversed.get(i + 1).altitude().orElse(0) < maxAlt) {
                cutIndex = i;
                break;
            }
        }

        List<Waypoint> trimmed = new ArrayList<>(reversed.subList(cutIndex, reversed.size()));
        Collections.reverse(trimmed);
        return new FinishedClimb(trimmed, fc.ascent(), fc.descent());
    }

    private List<FinishedClimb> cleanup(List<FinishedClimb> climbs) {
        if (climbs.isEmpty()) {
            return List.of();
        }

        FinishedClimb winner = climbs.getFirst();
        List<FinishedClimb> remaining = new ArrayList<>();
        for (int i = 1; i < climbs.size(); i++) {
            FinishedClimb fc = climbs.get(i);
            if (overlaps(winner, fc)) {
                if (fc.ascent() > winner.ascent()) {
                    winner = fc;
                }
            } else {
                remaining.add(fc);
            }
        }

        List<FinishedClimb> result = new ArrayList<>();
        result.add(winner);
        result.addAll(cleanup(remaining));
        return result;
    }

    private static boolean overlaps(FinishedClimb fc1, FinishedClimb fc2) {
        return !Collections.disjoint(fc1.wps(), fc2.wps());
    }

    private ClimbPointer toClimbPointer(FinishedClimb fc) {
        int startMeters = fc.wps().getFirst().distanceInMeter().orElse(0);
        int endMeters = fc.wps().getLast().distanceInMeter().orElse(0);
        Bounds bounds = Bounds.ofWaypoints(fc.wps());
        return new ClimbPointer(startMeters, endMeters, fc.ascent(), fc.descent(), bounds);
    }

    private sealed interface ClimbState permits OngoingClimb, FinishedClimb, AbandonedClimb {
    }

    private record OngoingClimb(List<Waypoint> wps, int ascent, int descent) implements ClimbState {
    }

    private record FinishedClimb(List<Waypoint> wps, int ascent, int descent) implements ClimbState {
    }

    private enum AbandonedClimb implements ClimbState {
        INSTANCE
    }
}
