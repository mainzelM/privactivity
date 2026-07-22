package net.privactivity.store.usecase.gettiles;

import java.util.List;

public record TileLegend(TileMetric metric,
                         Double min,
                         Double max,
                         List<Double> quantiles) {
}