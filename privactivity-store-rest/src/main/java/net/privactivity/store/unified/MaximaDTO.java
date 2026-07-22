package net.privactivity.store.unified;

import net.privactivity.domain.Activity;

public record MaximaDTO(double speedInMetersPerHour, double heartRate, double power, double altitude) {
    public MaximaDTO(Activity.Maxima maxima) {
        this(maxima.speedInMetersPerHour().orElse(0),
             maxima.heartRate().orElse(0),
             maxima.power().orElse(0),
             maxima.altitude().orElse(0));
    }
}