package net.privactivity.store.model;

import net.privactivity.domain.Activity;
import net.privactivity.store.adapter.ActivityRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapBasedActivityRepository implements ActivityRepository {


    private final Map<Long, Activity> activityMap;

    public MapBasedActivityRepository(Map<Long, Activity> activityMap) {
        this.activityMap = new HashMap<>(activityMap);
    }

    @Override
    public List<Activity> getAll(boolean attachWaypoints) {
        return new ArrayList<>(activityMap.values());
    }

    @Override
    public long addActivity(Activity activity) {
        activityMap.put(activity.id(), activity);
        return activity.id();
    }

    @Override
    public Activity getActivityById(long activityId, boolean includeWaypoints) {
        return activityMap.get(activityId);
    }

    @Override
    public long updateActivity(Activity activity, boolean updateWaypoints) {
        activityMap.put(activity.id(), activity);
        return activity.id();
    }

    @Override
    public void removeAll() {
        activityMap.clear();
    }

    @Override
    public void gc() {
        // No resources need to be reclaimed by the in-memory repository.
    }

    @Override
    public boolean containsActivityId(Long activityId) {
        return activityMap.containsKey(activityId);
    }
}
