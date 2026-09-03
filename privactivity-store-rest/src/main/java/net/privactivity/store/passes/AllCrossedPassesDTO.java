package net.privactivity.store.passes;

import net.privactivity.store.usecase.allcrossedpasses.AllCrossedPasses;
import net.privactivity.store.usecase.allcrossedpasses.PassCrossing;

import java.time.ZonedDateTime;
import java.util.List;

public record AllCrossedPassesDTO(List<PassCrossingDTO> passCrossings) {
    public static AllCrossedPassesDTO from(AllCrossedPasses allCrossedPasses) {
        return new AllCrossedPassesDTO(allCrossedPasses.allPassCrossings().stream()
                                                       .map(PassCrossingDTO::from)
                                                       .toList());
    }

    public record PassCrossingDTO(String passName, String passCountry, long firstCrossingActivityId,
                                  ZonedDateTime firstCrossingDate, long lastCrossingActivityId,
                                  ZonedDateTime lastCrossingDate) {
        public static PassCrossingDTO from(PassCrossing passCrossing) {
            return new PassCrossingDTO(passCrossing.mountainPass().name(),
                                       passCrossing.mountainPass().country(),
                                       passCrossing.firstCrossing().id(),
                                       passCrossing.firstCrossing().start(),
                                       passCrossing.lastCrossing().id(),
                                       passCrossing.lastCrossing().start());
        }
    }
}
