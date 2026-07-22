package net.privactivity.store.usecase.importactivities.adapter;

import java.nio.file.Path;
import java.util.List;

public interface ActivitiesJsonReader {
    List<GarminActivity> read(Path dir);
}
