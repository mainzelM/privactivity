package net.privactivity.store.usecase.importactivities.adapter;

import net.privactivity.domain.Activity;
import java.io.InputStream;

public interface TcxImporter {
    String DEFAULT_TITLE_PREFIX = "TCX from ";

    Activity importTcx(InputStream is);
}
