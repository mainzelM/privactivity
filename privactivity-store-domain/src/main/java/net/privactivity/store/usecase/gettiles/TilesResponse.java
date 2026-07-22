package net.privactivity.store.usecase.gettiles;

import java.time.Instant;
import java.util.List;

public record TilesResponse(TileMetric metric,
                            TileLegend legend,
                            double tileWidthDegrees,
                            double tileHeightDegrees,
                            Instant generatedAt,
                            List<TileSummary> tiles) {
}