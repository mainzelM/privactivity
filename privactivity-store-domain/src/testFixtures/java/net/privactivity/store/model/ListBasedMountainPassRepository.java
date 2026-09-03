package net.privactivity.store.model;

import net.privactivity.store.adapter.MountainPassRepository;
import net.privactivity.store.usecase.crossedpasses.MountainPass;

import java.util.List;

public class ListBasedMountainPassRepository implements MountainPassRepository {

    private final List<MountainPass> mountainPasses;

    public ListBasedMountainPassRepository(List<MountainPass> mountainPasses) {
        this.mountainPasses = mountainPasses;
    }

    @Override
    public List<MountainPass> getAllPasses() {
        return mountainPasses;
    }
}
