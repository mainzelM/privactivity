package net.privactivity.store.usecase.export.adapter;

import net.privactivity.domain.Activity;

public interface GpxExporter {
    String exportActivityAsGpx(Activity activity);
}
