package net.privactivity.store.usecase.rebuildtiles;

import net.privactivity.store.usecase.gettiles.TileSummary;
import java.time.Instant;
import java.util.List;

public record TileSnapshot(double tileWidthDegrees,
                           double tileHeightDegrees,
                           Instant generatedAt,
                           List<TileSummary> tiles) {

    public static TileSnapshot empty(double tileWidthDegrees, double tileHeightDegrees) {
        return new TileSnapshot(tileWidthDegrees, tileHeightDegrees, null, List.of());
    }
}