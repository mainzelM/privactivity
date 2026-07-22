package net.privactivity.store.usecase.maxpower;

import net.privactivity.domain.Waypoint;

import java.time.ZonedDateTime;

public record MaxPowerEvent(int watts, Waypoint wp, long activityId,
                            ZonedDateTime timestamp) {
}
