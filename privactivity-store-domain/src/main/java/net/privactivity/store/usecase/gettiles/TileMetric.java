package net.privactivity.store.usecase.gettiles;

public enum TileMetric {
    ACTIVITY_COUNT,
    MAX_POWER,
    MAX_HEART_RATE,
    AVERAGE_POWER,
    AVERAGE_HEART_RATE;

    public static TileMetric fromParameter(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVITY_COUNT;
        }

        return switch (value.trim().toLowerCase()) {
            case "activitycount", "activity_count", "count" -> ACTIVITY_COUNT;
            case "maxpower", "max_power" -> MAX_POWER;
            case "maxheartrate", "max_heart_rate", "heartratemax" -> MAX_HEART_RATE;
            case "averagepower", "avgpower", "average_power" -> AVERAGE_POWER;
            case "averageheartrate", "avgheartrate", "average_heart_rate" -> AVERAGE_HEART_RATE;
            default -> throw new IllegalArgumentException("Unsupported tile metric: " + value);
        };
    }
}