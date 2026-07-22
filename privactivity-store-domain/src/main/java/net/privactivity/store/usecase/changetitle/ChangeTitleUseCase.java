package net.privactivity.store.usecase.changetitle;

import net.privactivity.domain.Maybe;
import net.privactivity.store.adapter.MetaData;
import net.privactivity.store.adapter.MetaDataRepository;

public class ChangeTitleUseCase {

    private final MetaDataRepository metaDataRepository;

    public ChangeTitleUseCase(MetaDataRepository metaDataRepository) {
        this.metaDataRepository = metaDataRepository;
    }

    public void changeTitle(long activityId, String newTitle) {
        Maybe<MetaData> metaData = metaDataRepository.getMetaDataForActivity(activityId);
        MetaData changedMetaData = metaData.orElseGet(MetaData::empty).withTitle(newTitle);
        metaDataRepository.setMetaData(activityId, changedMetaData);
    }
}
