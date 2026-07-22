package net.privactivity.store.adapter;

import net.privactivity.domain.Activity;
import java.util.List;

public interface ActivityRepository {
    List<Activity> getAll(boolean attachWaypoints);

    long addActivity(Activity activity);

    Activity getActivityById(long activityId, boolean includeWaypoints);

    long updateActivity(Activity activity, boolean updateWaypoints);

    void removeAll();

    void gc();

    boolean containsActivityId(Long activityId);
}
