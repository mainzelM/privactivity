package net.privactivity.store.unified;

import net.privactivity.domain.Activity;

public record AveragesDTO(double speedInMetersPerHour, double cadence, double heartRate, double power) {
    public AveragesDTO(Activity.Averages averages) {
        this(averages.speedInMetersPerHour().orElse(0),
             averages.cadence().orElse(0),
             averages.heartRate().orElse(0),
             averages.power().orElse(0));
    }
}