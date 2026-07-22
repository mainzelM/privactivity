package net.privactivity.store.usecase.importactivities.adapter;

import net.privactivity.domain.Activity;
import java.io.InputStream;

public interface GpxImporter {
    Activity importGpxAsActivity(InputStream is, long id);
}
