package net.privactivity.store.usecase.importactivities.adapter;

import net.privactivity.domain.Activity;
import java.io.IOException;
import java.io.InputStream;

public interface FitDecoder {
    Activity extractFit(InputStream is, long id) throws IOException;
}
