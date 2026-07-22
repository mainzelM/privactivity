package net.privactivity.store.jsonrepo.metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import net.privactivity.domain.Maybe;
import net.privactivity.store.adapter.MetaData;
import net.privactivity.store.adapter.MetaDataRepository;
import net.privactivity.store.jsonrepo.JsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetaDataRepositoryImpl extends JsonRepository<Map<Long, MetaData>> implements MetaDataRepository {


    @Autowired
    public MetaDataRepositoryImpl(String storageDirectory) {
        super("metadata.json", storageDirectory);
    }

    public List<MetaData> getAll() {
        return new ArrayList<>(cache.values());
    }

    public MetaData get(long activityId) {
        return cache.get(activityId);
    }

    @Override
    public void setMetaData(long activityId, MetaData metaData) {
        Map<Long, MetaData> newSnapshot = new HashMap<>(cache);
        newSnapshot.put(activityId, metaData);
        replaceSnapshot(newSnapshot);
    }


    @Override
    public Maybe<MetaData> getMetaDataForActivity(long activityId) {
        return Maybe.nullAsNone(cache.get(activityId));
    }

    @Override
    protected Map<Long, MetaData> makeEmptySnapshot() {
        return new HashMap<>();
    }

    @Override
    protected TypeReference<Map<Long, MetaData>> snapshotType() {
        return new TypeReference<>() {
        };
    }
}
