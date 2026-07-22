package net.privactivity.store.repo.climb;

import net.privactivity.domain.Climb;
import net.privactivity.domain.ClimbPointer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public record ClimbDataRoot(Map<Long, Set<ClimbPointer>> climbPointers, Set<Climb> climbs) {
    public ClimbDataRoot() {
        this(new HashMap<>(), new HashSet<>());
    }
}
