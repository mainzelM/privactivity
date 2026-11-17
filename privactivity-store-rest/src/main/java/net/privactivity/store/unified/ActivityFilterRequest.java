package net.privactivity.store.unified;

import net.privactivity.store.model.SortDirection;

import java.time.DayOfWeek;

public record ActivityFilterRequest(
        Boolean includeWaypoints,
        Integer minDistance,
        Integer maxDistance,
        Integer minAscent,
        Integer maxAscent,
        Double lat,
        Double lon,
        String title,
        String sortBy,
        SortDirection sortDirection,
        String tileId,
        DayOfWeek dayOfWeek) {
}
