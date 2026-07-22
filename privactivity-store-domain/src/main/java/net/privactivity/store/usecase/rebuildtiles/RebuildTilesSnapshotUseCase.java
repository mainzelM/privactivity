package net.privactivity.store.usecase.rebuildtiles;

import net.privactivity.domain.Activity;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.adapter.TilesRepository;
import net.privactivity.store.usecase.gettiles.TileBounds;
import net.privactivity.store.usecase.gettiles.TileSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RebuildTilesSnapshotUseCase {
    private static final Logger logger = LoggerFactory.getLogger(RebuildTilesSnapshotUseCase.class);

    private final ActivityRepository activityRepository;
    private final TilesProperties tilesProperties;
    private final TilesRepository tilesRepository;
    private final Map<TileKey, TileAccumulator> accumulators = new HashMap<>();

    public RebuildTilesSnapshotUseCase(ActivityRepository activityRepository,
                                       TilesProperties tilesProperties,
                                       TilesRepository tilesRepository) {
        this.activityRepository = activityRepository;
        this.tilesProperties = tilesProperties;
        this.tilesRepository = tilesRepository;
    }

    public void rebuildSnapshot() {
        long startedAt = System.currentTimeMillis();
        List<Activity> activities = activityRepository.getAll(false);

        TileSnapshot snapshot = buildSnapshot(activities);
        tilesRepository.replaceSnapshot(snapshot);

        logger.info("Rebuilt tiles snapshot for {} activities into {} tiles in {} ms",
                    activities.size(),
                    snapshot.tiles().size(),
                    System.currentTimeMillis() - startedAt);
    }

    /*VisibleForTesting*/ TileSnapshot buildSnapshot(List<Activity> activities) {
        activities.stream()
                  .map(this::withWaypoint)
                  .forEach(this::accumulateActivity);

        List<TileSummary> tiles = accumulators.values()
                                              .stream()
                                              .map(TileAccumulator::toTileSummary)
                                              .sorted(Comparator.comparing(TileSummary::tileId))
                                              .toList();

        return new TileSnapshot(tilesProperties.tileWidthDegrees(), tilesProperties.tileHeightDegrees(),
                                Instant.now(), tiles);
    }

    private void accumulateActivity(Activity activity) {
        for (Waypoint waypoint : activity.waypoints()) {
            if (!waypoint.latlon().isPresent()) {
                continue;
            }
            LatLon latLon = waypoint.latlon().orThrow();
            TileKey tileKey = TileKey.of(latLon.lat(), latLon.lon(),
                                         tilesProperties.tileWidthDegrees(), tilesProperties.tileHeightDegrees());
            accumulators.computeIfAbsent(tileKey, TileAccumulator::new)
                        .accept(activity, waypoint);
        }
    }

    private Activity withWaypoint(Activity activity) {
        return activityRepository.getActivityById(activity.id(), true);
    }


    private static final class TileAccumulator {
        private final TileKey tileKey;
        private final Set<Long> activityIdsInTile = new HashSet<>();
        private ZonedDateTime firstActivityDate;
        private ZonedDateTime latestActivityDate;
        private Long firstActivityId;
        private Long latestActivityId;
        private int maxPower;
        private int maxHeartRate;
        private long totalPower;
        private int powerSamples;
        private long totalHeartRate;
        private int heartRateSamples;

        private TileAccumulator(TileKey tileKey) {
            this.tileKey = tileKey;
        }

        private void accept(Activity activity, Waypoint waypoint) {
            if (activityIdsInTile.add(activity.id())) {
                if (firstActivityDate == null || activity.start().isBefore(firstActivityDate)) {
                    firstActivityDate = activity.start();
                    firstActivityId = activity.id();
                }
                if (latestActivityDate == null || activity.start().isAfter(latestActivityDate)) {
                    latestActivityDate = activity.start();
                    latestActivityId = activity.id();
                }
            }

            maxPower = max(maxPower, waypoint.power());
            maxHeartRate = max(maxHeartRate, waypoint.heartRate());
            addAverage(waypoint.power(), true);
            addAverage(waypoint.heartRate(), false);
        }

        private int max(int current, Maybe<Integer> maybeValue) {
            return Math.max(current, maybeValue.orElse(0));
        }

        private void addAverage(Maybe<Integer> maybeValue, boolean power) {
            if (!maybeValue.isPresent()) {
                return;
            }
            int value = maybeValue.orThrow();
            if (power) {
                totalPower += value;
                powerSamples++;
            } else {
                totalHeartRate += value;
                heartRateSamples++;
            }
        }

        private TileSummary toTileSummary() {
            return new TileSummary(tileKey.tileId(),
                                   tileKey.bounds(),
                                   firstActivityId,
                                   latestActivityId,
                                   maxPower,
                                   maxHeartRate,
                                   powerSamples > 0 ? (double) totalPower / powerSamples : null,
                                   heartRateSamples > 0 ? (double) totalHeartRate / heartRateSamples : null,
                                   activityIdsInTile.stream().sorted().toList());
        }
    }

    private record TileKey(int latIndex, int lonIndex, double tileWidthDegrees, double tileHeightDegrees) {
        private static TileKey of(double latitude, double longitude, double tileWidthDegrees,
                                  double tileHeightDegrees) {
            int latIndex = (int) Math.floor(latitude / tileHeightDegrees);
            int lonIndex = (int) Math.floor(longitude / tileWidthDegrees);
            return new TileKey(latIndex, lonIndex, tileWidthDegrees, tileHeightDegrees);
        }

        private String tileId() {
            return latIndex + ":" + lonIndex;
        }

        private TileBounds bounds() {
            double south = latIndex * tileHeightDegrees;
            double north = south + tileHeightDegrees;
            double west = lonIndex * tileWidthDegrees;
            double east = west + tileWidthDegrees;
            return new TileBounds(south, north, west, east);
        }
    }
}
