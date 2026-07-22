package net.privactivity.store.unified;

import net.privactivity.store.usecase.findActivities.SortDirection;

public record ActivityFilterRequest(
        Boolean includeWaypoints,
        Integer minDistance,
        Integer maxDistance,
        Integer minAscent,
        Integer maxAscent,
        Double lat,
        Double lon,
        Integer radius,
        String title,
        String sortBy,
        SortDirection sortDirection,
        String tileId) {
}
