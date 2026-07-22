package net.privactivity.store.usecase.gettiles;

import java.util.List;

public record TileSummary(String tileId,
                          TileBounds bounds,
                          Long firstActivityId,
                          Long latestActivityId,
                          int maxPower,
                          int maxHeartRate,
                          Double averagePower,
                          Double averageHeartRate,
                          List<Long> activityIds) {

    public TileSummary {
        activityIds = activityIds == null ? List.of() : List.copyOf(activityIds);
    }

    public int activityCount() {
        return activityIds.size();
    }

    public Double metricValue(TileMetric metric) {
        return switch (metric) {
            case ACTIVITY_COUNT -> (double) activityCount();
            case MAX_POWER -> maxPower > 0 ? (double) maxPower : null;
            case MAX_HEART_RATE -> maxHeartRate > 0 ? (double) maxHeartRate : null;
            case AVERAGE_POWER -> averagePower;
            case AVERAGE_HEART_RATE -> averageHeartRate;
        };
    }
}