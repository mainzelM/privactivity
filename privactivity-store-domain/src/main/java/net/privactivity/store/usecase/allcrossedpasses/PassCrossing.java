package net.privactivity.store.usecase.allcrossedpasses;

import net.privactivity.domain.Activity;
import net.privactivity.store.usecase.crossedpasses.MountainPass;

public record PassCrossing(MountainPass mountainPass, Activity firstCrossing, Activity lastCrossing) {
}
