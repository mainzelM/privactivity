package net.privactivity.store.unified;

import com.fasterxml.jackson.annotation.JsonFormat;
import net.privactivity.domain.Activity;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record ActivityDTO(
        long id,
        String title,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") ZonedDateTime start,
        List<WaypointDTO> waypoints,
        TotalsDTO totals,
        AveragesDTO averages,
        MaximaDTO maxima,
        BoundsDTO bounds,
        SummaryDTO summary) {

    public ActivityDTO(Activity activity) {
        this(
                activity.id(),
                activity.title(),
                activity.start(),
                activity.waypoints().stream().map(WaypointDTO::new).collect(Collectors.toList()),
                new TotalsDTO(activity.totals()),
                new AveragesDTO(activity.averages()),
                new MaximaDTO(activity.maxima()),
                new BoundsDTO(activity.bounds()),
                new SummaryDTO(activity.summary()));
    }
}
