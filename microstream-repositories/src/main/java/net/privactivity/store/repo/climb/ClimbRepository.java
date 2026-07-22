package net.privactivity.store.repo.climb;

import net.privactivity.domain.Climb;
import net.privactivity.domain.ClimbPointer;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;



import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

@Service
public class ClimbRepository {

    private final EmbeddedStorageManager storageManager;
    private final ClimbDataRoot dataRoot;

    @Autowired
    public ClimbRepository(@Qualifier("climb_storage") EmbeddedStorageManager climbStorageManager) {
        this.storageManager = climbStorageManager;
        ClimbDataRoot root = (ClimbDataRoot) climbStorageManager.root();
        if (root == null) {
            root = new ClimbDataRoot();
            storageManager.setRoot(root);
            storageManager.storeRoot();
        }
        this.dataRoot = root;
    }

    public Set<Climb> getAllClimbs() {
        return unmodifiableSet(dataRoot.climbs());
    }

    public Map<Long, Set<ClimbPointer>> getAllClimbPointers() {
        return unmodifiableMap(dataRoot.climbPointers());
    }

    public Set<ClimbPointer> climbsOf(long activityId) {
        return unmodifiableSet(dataRoot.climbPointers().getOrDefault(activityId, emptySet()));
    }

    public long addClimb(Climb climb) {
        dataRoot.climbs().add(climb);
        return storageManager.store(dataRoot.climbs());
    }

    public long addActivityClimbPointers(long activityId, Set<ClimbPointer> climbs) {
        dataRoot.climbPointers().put(activityId, climbs);
        return storageManager.store(dataRoot.climbPointers());
    }

    public void removeAllClimbPointers() {
        dataRoot.climbPointers().clear();
        storageManager.store(dataRoot.climbPointers());
    }

    public void removeAllClimbs() {
        dataRoot.climbs().clear();
        storageManager.store(dataRoot.climbs());
    }
}
