package net.privactivity.store.model;

import net.privactivity.domain.Activity;
import net.privactivity.store.adapter.ActivityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapBasedActivityRepository implements ActivityRepository {


    private final Map<Long, Activity> activityMap;

    public MapBasedActivityRepository(Map<Long, Activity> activityMap) {
        this.activityMap = activityMap;
    }

    @Override
    public List<Activity> getAll(boolean attachWaypoints) {
        return new ArrayList<>(activityMap.values());
    }

    @Override
    public long addActivity(Activity activity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Activity getActivityById(long activityId, boolean includeWaypoints) {
        return activityMap.get(activityId);
    }

    @Override
    public long updateActivity(Activity activity, boolean updateWaypoints) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void gc() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsActivityId(Long activityId) {
        return activityMap.containsKey(activityId);
    }
}
