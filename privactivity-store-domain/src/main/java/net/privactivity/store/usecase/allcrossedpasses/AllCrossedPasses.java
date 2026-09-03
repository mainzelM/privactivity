package net.privactivity.store.usecase.allcrossedpasses;

import net.privactivity.domain.Activity;
import net.privactivity.store.usecase.crossedpasses.MountainPass;
import java.util.ArrayList;
import java.util.List;

public record AllCrossedPasses(List<PassCrossing> allPassCrossings) {

    public AllCrossedPasses(List<PassCrossing> allPassCrossings) {
        this.allPassCrossings = List.copyOf(allPassCrossings);
    }

    public AllCrossedPasses() {
        this(List.of());
    }


    public AllCrossedPasses noteCrossing(Activity activity, MountainPass mountainPass) {
        for (PassCrossing passCrossing : allPassCrossings) {
            if (passCrossing.mountainPass().equals(mountainPass)) {
                if (activity.start().isBefore(passCrossing.firstCrossing().start())) {
                    PassCrossing newCrossing = new PassCrossing(mountainPass, activity, passCrossing.lastCrossing());
                    return replaceCrossing(passCrossing, newCrossing);
                } else if (activity.start().isAfter(passCrossing.lastCrossing().start())) {
                    PassCrossing newCrossing = new PassCrossing(mountainPass, passCrossing.firstCrossing(), activity);
                    return replaceCrossing(passCrossing, newCrossing);
                } else {
                    return this;
                }
            }
        }
        List<PassCrossing> newCrossings = new ArrayList<>(allPassCrossings);
        PassCrossing newPassCrossing = new PassCrossing(mountainPass, activity, activity);
        newCrossings.add(newPassCrossing);
        return new AllCrossedPasses(newCrossings);
    }

    private AllCrossedPasses replaceCrossing(PassCrossing existingCrossing, PassCrossing newCrossing) {
        List<PassCrossing> newCrossings = new ArrayList<>(allPassCrossings);
        newCrossings.remove(existingCrossing);
        newCrossings.add(newCrossing);
        return new AllCrossedPasses(newCrossings);
    }
}
