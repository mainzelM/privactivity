package net.privactivity.store.maxpower;

import com.fasterxml.jackson.annotation.JsonFormat;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.usecase.maxpower.MaxPowerEvent;
import java.time.ZonedDateTime;

public record MaxPowerEventDTO(int watts, Waypoint wp, long activityId,
                               @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm") ZonedDateTime timestamp) {
    public MaxPowerEventDTO(MaxPowerEvent value) {
        this(value.watts(), value.wp(), value.activityId(), value.timestamp());
    }
}