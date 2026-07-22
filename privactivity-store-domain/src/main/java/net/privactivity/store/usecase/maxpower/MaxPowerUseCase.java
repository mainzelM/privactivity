package net.privactivity.store.usecase.maxpower;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.adapter.ActivityRepository;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MaxPowerUseCase {
    private final ActivityRepository activityRepository;
    private final List<Duration> durations;

    public MaxPowerUseCase(ActivityRepository activityRepository, Integer... durationsInMinutes) {
        this.activityRepository = activityRepository;
        durations = Stream.of(durationsInMinutes)
                          .map(Duration::ofMinutes)
                          .toList();
    }

    public MaxPower maxPowerAll(Collection<MaxPower> maxPowers) {
        final Map<Duration, MaxPowerEvent> overAll = new HashMap<>();
        maxPowers.forEach(maxPower -> {
            maxPower.maxPowers().forEach((duration, event) -> {
                if (overAll.containsKey(duration)) {
                    int overAllMax = overAll.get(duration).watts();
                    if (overAllMax < event.watts()) {
                        overAll.put(duration, event);
                    }
                } else {
                    overAll.put(duration, event);
                }
            });
        });
        return new MaxPower(overAll);
    }

    public MaxPower maxPowerOne(long activityId) {
        Activity activity = activityRepository.getActivityById(activityId, true);
        return maxPowerOne(activity);

    }

    public MaxPower maxPowerOne(Activity activity) {
        ArrayList<MaxPowerForWindow> windows = new ArrayList<>(durations.size());


        durations.forEach(d -> windows.add(new MaxPowerForWindow(d, activity)));


        int lastEnd = 0;

        for (Waypoint wp : activity.waypoints()) {
            int duration = wp.secondsSinceStart() - lastEnd;

            for (MaxPowerForWindow window : windows) {
                window.update(wp, duration);
            }

            lastEnd = wp.secondsSinceStart();
        }
        Map<Duration, MaxPowerEvent> maxPowers = new HashMap<>(durations.size());
        for (MaxPowerForWindow window : windows) {
            if (window.getMax().isPresent()) {
                maxPowers.put(window.getDuration(), window.getMax().get());
            }

        }
        return new MaxPower(maxPowers);
    }

    public MaxPower maxPowerAll() {
        List<net.privactivity.store.usecase.maxpower.MaxPower> allMaxPowers =
                activityRepository.getAll(false).parallelStream()
                                  .flatMap(this::maxPowerOf)
                                  .toList();
        return maxPowerAll(allMaxPowers);
    }

    private Stream<MaxPower> maxPowerOf(Activity a) {
        try {
            Activity activity = activityRepository.getActivityById(a.id(), true);
            net.privactivity.store.usecase.maxpower.MaxPower maxPower = maxPowerOne(activity);
            activity.releaseWaypoints();
            return Stream.of(maxPower);
        } catch (Exception e) {
            System.err.println(a.id() + " " + e.getMessage());
            return Stream.empty();
        }
    }

    private static class MaxPowerForWindow {
        private final Activity activity;
        private int max = 0;
        private Waypoint maxWaypoint;
        private final Deque<Integer> windowValues;
        private final Duration duration;
        private final int windowSize;


        public MaxPowerForWindow(Duration duration, Activity activity) {
            this.activity = activity;
            this.duration = duration;
            this.windowSize = (int) duration.toSeconds();
            this.windowValues = new ArrayDeque<>(windowSize);
        }

        void update(Waypoint wp, int wpDurationInSeconds) {
            int sanitizedWatts = sanitizedWatts(wp);
            for (int i = 0; i < wpDurationInSeconds; i++) {
                if (windowValues.size() >= windowSize) {
                    windowValues.removeFirst();
                }
                windowValues.add(sanitizedWatts);
            }

            if (windowValues.size() >= windowSize) {
                int mean = calculateMean();
                if (mean > max) {
                    max = mean;
                    maxWaypoint = wp;
                }
            }
        }

        private int calculateMean() {
            if (windowValues.isEmpty())
                return 0;

            long sum = 0;
            for (int value : windowValues) {
                sum += value;
            }
            return (int) Math.round((double) sum / windowValues.size());
        }

        private int sanitizedWatts(Waypoint wp) {
            int w = wp.power().orElse(100);
            return w > 1500 ? 100 : w; // TODO: add test
        }

        Optional<MaxPowerEvent> getMax() {
            if (maxWaypoint == null) {
                return Optional.empty();
            } else {
                return Optional.of(new MaxPowerEvent(max, maxWaypoint, activity.id(),
                                                     activity.start().plusSeconds(maxWaypoint.secondsSinceStart())));
            }
        }

        public Duration getDuration() {
            return duration;
        }
    }
}
