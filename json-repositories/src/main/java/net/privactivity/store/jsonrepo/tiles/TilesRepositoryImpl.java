package net.privactivity.store.jsonrepo.tiles;

import com.fasterxml.jackson.core.type.TypeReference;
import net.privactivity.store.adapter.TilesRepository;
import net.privactivity.store.jsonrepo.JsonRepository;
import net.privactivity.store.usecase.gettiles.TileSummary;
import net.privactivity.store.usecase.rebuildtiles.TileSnapshot;
import net.privactivity.store.usecase.rebuildtiles.TilesProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TilesRepositoryImpl extends JsonRepository<TileSnapshot> implements TilesRepository {

    private final TilesProperties properties;


    public TilesRepositoryImpl(TilesProperties properties,
                               @Value("${privactivity.store.json.storage-directory}") String storageDirectory) {
        this.properties = properties;
        super("tiles.json", storageDirectory);
    }


    @Override
    public synchronized TileSnapshot getSnapshot() {
        return cache;
    }


    @Override
    public TileSummary getTileById(String tileId) {
        return cache.tiles().stream()
                    .filter(tile -> tile.tileId().equals(tileId))
                    .findAny()
                    .orElseThrow();
    }


    @Override
    protected TileSnapshot makeEmptySnapshot() {
        return TileSnapshot.empty(properties.tileWidthDegrees(), properties.tileHeightDegrees());
    }

    @Override
    protected TypeReference<TileSnapshot> snapshotType() {
        return new TypeReference<>() {
        };
    }
}