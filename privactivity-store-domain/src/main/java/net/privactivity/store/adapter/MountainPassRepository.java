package net.privactivity.store.adapter;

import net.privactivity.store.usecase.crossedpasses.MountainPass;
import java.util.List;

public interface MountainPassRepository {
    List<MountainPass> getAllPasses();
}
