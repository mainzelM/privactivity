package net.privactivity.store.usecase.maxpower;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public record MaxPower(Map<Duration, MaxPowerEvent> maxPowers) {
    public Optional<MaxPowerEvent> getForMinutes(int minutes) {
        return Optional.ofNullable(maxPowers.get(Duration.ofMinutes(minutes)));
    }
}
