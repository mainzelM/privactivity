package net.privactivity.townfinder.adapter;

import net.privactivity.domain.Activity;
import net.privactivity.domain.Maybe;
import net.privactivity.store.usecase.townfinder.adapter.ActivityTownsRepository;
import net.privactivity.store.usecase.townfinder.model.Town;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import static net.privactivity.domain.Maybe.none;
import static net.privactivity.domain.Maybe.some;

/**
 * Map-based stub implementation of ActivityTownsRepository for testing.
 * <p>
 * This test fixture provides an in-memory implementation that can be used
 * in unit tests to avoid dependencies on external storage systems.
 */
public class MapBasedActivityTownsRepository implements ActivityTownsRepository {

    private final Map<Long, List<Town>> activityTownsMap = new HashMap<>();
    private final Map<Long, List<String>> activityTownNamesMap = new HashMap<>();

    @Override
    public Maybe<List<Town>> townsOfActivity(Activity activity) {
        List<Town> towns = activityTownsMap.get(activity.id());
        return towns != null ? some(towns) : none();
    }

    @Override
    public void setActivityTowns(Activity activity, List<String> mainTowns) {
        List<Town> towns = mainTowns.stream().map(Town::new).toList();
        activityTownsMap.put(activity.id(), towns);
        activityTownNamesMap.put(activity.id(), mainTowns);
    }

    // Test helper methods

    /**
     * Helper method for test setup - allows directly setting towns as objects
     */
    public void setStoredTowns(Activity activity, List<Town> towns) {
        activityTownsMap.put(activity.id(), towns);
    }

    /**
     * Helper method for test verification - gets the original town names as strings
     */
    public List<String> getStoredTownNames(Activity activity) {
        return activityTownNamesMap.get(activity.id());
    }

    /**
     * Helper method to clear all stored data (useful for test cleanup)
     */
    public void clear() {
        activityTownsMap.clear();
        activityTownNamesMap.clear();
    }

    /**
     * Helper method to check if any towns are stored for an activity
     */
    public boolean hasTowns(Activity activity) {
        return activityTownsMap.containsKey(activity.id());
    }

    /**
     * Helper method to get the number of stored activities
     */
    public int getStoredActivitiesCount() {
        return activityTownsMap.size();
    }
}