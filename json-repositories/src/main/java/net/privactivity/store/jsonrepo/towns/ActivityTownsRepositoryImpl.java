package net.privactivity.store.jsonrepo.towns;

import com.fasterxml.jackson.core.type.TypeReference;
import net.privactivity.domain.Activity;
import net.privactivity.domain.Maybe;
import net.privactivity.store.jsonrepo.JsonRepository;
import net.privactivity.store.usecase.townfinder.adapter.ActivityTownsRepository;
import net.privactivity.store.usecase.townfinder.model.Town;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityTownsRepositoryImpl extends JsonRepository<Map<Long, List<Town>>> implements ActivityTownsRepository {

    public ActivityTownsRepositoryImpl(@Value("${privactivity.store.json.storage-directory}") String storageDirectory) {
        super("towns.json", storageDirectory);
    }

    @Override
    public Maybe<List<Town>> townsOfActivity(Activity activity) {
        return Maybe.nullAsNone(cache.get(activity.id()));
    }

    @Override
    public synchronized void setActivityTowns(Activity activity, List<String> mainTowns) {
        Map<Long, List<Town>> newSnapshot = new HashMap<>(cache);
        newSnapshot.put(activity.id(), mainTowns.stream().map(Town::new).toList());
        replaceSnapshot(newSnapshot);
    }

    @Override
    protected Map<Long, List<Town>> makeEmptySnapshot() {
        return new HashMap<>();
    }

    @Override
    protected TypeReference<Map<Long, List<Town>>> snapshotType() {
        return new TypeReference<>() {
        };
    }
}
