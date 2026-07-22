package net.privactivity.store.adapter;

import net.privactivity.store.usecase.gettiles.TileSummary;
import net.privactivity.store.usecase.rebuildtiles.TileSnapshot;

public interface TilesRepository {
    TileSnapshot getSnapshot();

    void replaceSnapshot(TileSnapshot snapshot);

    TileSummary getTileById(String tileId);
}
