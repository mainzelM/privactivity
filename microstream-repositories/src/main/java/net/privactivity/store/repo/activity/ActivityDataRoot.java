package net.privactivity.store.repo.activity;

import net.privactivity.domain.Activity;
import org.eclipse.serializer.reference.Lazy;
import java.util.HashMap;
import java.util.Map;


class ActivityDataRoot {
    private final Map<Long, Activity> activities = new HashMap<>();
    private final Map<Long, Lazy<byte[]>> waypoints = new HashMap<>();

    Map<Long, Activity> getActivities() {
        return this.activities;
    }

    Map<Long, Lazy<byte[]>> getWaypoints() {
        return waypoints;
    }

    @Override
    public String toString() {
        return "Root ";
    }
}