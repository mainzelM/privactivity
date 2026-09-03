package net.privactivity.store.passes;

import net.privactivity.store.usecase.crossedpasses.CrossedPasses;
import net.privactivity.store.usecase.crossedpasses.MountainPass;

public record CrossedPassesDTO(java.util.List<MountainPass> crossedPasses) {
    public static CrossedPassesDTO from(CrossedPasses crossedPasses) {
        return new CrossedPassesDTO(crossedPasses.mountainPasses());
    }
}
