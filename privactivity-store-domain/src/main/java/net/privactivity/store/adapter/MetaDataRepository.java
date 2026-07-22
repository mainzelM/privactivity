package net.privactivity.store.adapter;

import net.privactivity.domain.Maybe;

public interface MetaDataRepository {

    void setMetaData(long activityId, MetaData metaData);

    Maybe<MetaData> getMetaDataForActivity(long activityId);
}
