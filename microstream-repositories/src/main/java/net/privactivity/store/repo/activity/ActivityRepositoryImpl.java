package net.privactivity.store.repo.activity;


import net.privactivity.domain.Activity;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.adapter.ActivityRepository;
import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.TypedSerializer;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class ActivityRepositoryImpl implements ActivityRepository {
    private static final Logger logger = LoggerFactory.getLogger(ActivityRepositoryImpl.class);
    private final EmbeddedStorageManager storageManager;
    private final ActivityDataRoot dataRoot;

    @Autowired
    public ActivityRepositoryImpl(@Qualifier("activityStorageManager") EmbeddedStorageManager fitStorageManager) {
        this.storageManager = fitStorageManager;
        ActivityDataRoot root = (ActivityDataRoot) storageManager.root();
        if (root == null) {
            root = new ActivityDataRoot();
            storageManager.setRoot(root);
            storageManager.storeRoot();
        }
        this.dataRoot = root;
    }

    private Serializer<byte[]> newSerializer() {
        return TypedSerializer.Bytes(SerializerFoundation.New());
    }

    private byte[] serializeWaypoints(List<Waypoint> waypoints) {
        try (Serializer<byte[]> serializer = newSerializer()) {
            return serializer.serialize(new ArrayList<>(waypoints));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize waypoints", e);
        }
    }

    private List<Waypoint> deserializeWaypoints(byte[] bytes) {
        try (Serializer<byte[]> serializer = newSerializer()) {
            return serializer.deserialize(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize waypoints", e);
        }
    }

    @Override
    public List<Activity> getAll(boolean attachWaypoints) {
        Collection<Activity> all = dataRoot.getActivities().values();
        if (attachWaypoints) {
            return all.stream()
                      .map(this::attachWaypoints)
                      .toList();
        } else {
            return new ArrayList<>(all);
        }
    }

    @Override
    public long addActivity(Activity activity) {
        return updateActivity(activity, true);
    }

    private void storeWaypoints(Activity activity, ActivityDataRoot dataRoot) {
        dataRoot.getWaypoints().put(activity.id(), Lazy.Reference(serializeWaypoints(activity.waypoints())));
        activity.releaseWaypoints();
    }

    @Override
    public Activity getActivityById(long activityId, boolean includeWaypoints) {
        Activity activity = Objects.requireNonNull(dataRoot.getActivities().get(activityId));
        if (includeWaypoints) {
            return attachWaypoints(activity);
        } else {
            return activity;
        }
    }

    private Activity attachWaypoints(Activity activity) {
        byte[] bytes = Objects.requireNonNull(dataRoot.getWaypoints().get(activity.id())).get();
        return activity.withWaypoints(deserializeWaypoints(bytes));
    }

    @Override
    public long updateActivity(Activity activity, boolean updateWaypoints) {
        if (updateWaypoints) {
            storeWaypoints(activity, dataRoot);
            dataRoot.getActivities().put(activity.id(), activity);
            return storageManager.storeAll(dataRoot.getActivities(), dataRoot.getWaypoints())[0];
        } else {
            dataRoot.getActivities().put(activity.id(), activity);
            return storageManager.store(dataRoot.getActivities());
        }
    }

    @Override
    public void removeAll() {
        dataRoot.getActivities().clear();
        dataRoot.getWaypoints().clear();
        storageManager.storeAll(dataRoot.getActivities(), dataRoot.getWaypoints());
        gc();
    }

    @Override
    public void gc() {
        storageManager.issueFullGarbageCollection();
        storageManager.issueFullFileCheck();
        logger.info("Storage GC finished");
    }

    @Override
    public boolean containsActivityId(Long activityId) {
        return dataRoot.getActivities().containsKey(activityId);
    }
}
