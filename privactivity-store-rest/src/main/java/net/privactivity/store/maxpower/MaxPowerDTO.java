package net.privactivity.store.maxpower;

import net.privactivity.store.usecase.maxpower.MaxPower;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record MaxPowerDTO(Map<Duration, MaxPowerEventDTO> maxPowers) {
    public MaxPowerDTO(MaxPower maxPower) {
        Map<Duration, MaxPowerEventDTO> dtoMap =
                maxPower.maxPowers().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                                  e -> new MaxPowerEventDTO(e.getValue())));
        this(dtoMap);
    }

    public Optional<MaxPowerEventDTO> getForMinutes(int minutes) {
        return Optional.ofNullable(maxPowers.get(Duration.ofMinutes(minutes)));
    }
}