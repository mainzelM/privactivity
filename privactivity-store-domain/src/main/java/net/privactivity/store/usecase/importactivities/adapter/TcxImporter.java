package net.privactivity.store.usecase.importactivities.adapter;

import net.privactivity.domain.Activity;
import java.io.InputStream;

public interface TcxImporter {
    Activity importTcx(InputStream is, long activityId);
}
