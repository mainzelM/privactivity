package net.privactivity.store.usecase.gettiles;

import net.privactivity.store.adapter.TilesRepository;
import net.privactivity.store.usecase.rebuildtiles.TileSnapshot;
import java.util.List;
import java.util.Objects;

public class GetTilesUseCase {


    private final TilesRepository tilesRepository;

    public GetTilesUseCase(TilesRepository tilesRepository) {
        this.tilesRepository = tilesRepository;
    }

    public TilesResponse getTiles(TileMetric metric, TileBounds bounds) {
        TileSnapshot snapshot = tilesRepository.getSnapshot();
        List<TileSummary> filteredTiles = filter(snapshot.tiles(), bounds);
        TileLegend legend = legendFor(filteredTiles, metric);

        return new TilesResponse(metric,
                                 legend,
                                 snapshot.tileWidthDegrees(),
                                 snapshot.tileHeightDegrees(),
                                 snapshot.generatedAt(),
                                 filteredTiles);
    }


    /*VisibleForTesting*/ TileLegend legendFor(List<TileSummary> tiles, TileMetric metric) {
        List<Double> values = tiles.stream()
                                   .map(tile -> tile.metricValue(metric))
                                   .filter(Objects::nonNull)
                                   .sorted()
                                   .toList();

        if (values.isEmpty()) {
            return new TileLegend(metric, null, null, List.of());
        }

        List<Double> quantiles = buildQuantiles(values, 20);

        return new TileLegend(metric,
                              values.getFirst(),
                              values.getLast(),
                              quantiles);
    }

    private List<TileSummary> filter(List<TileSummary> tiles, TileBounds bounds) {
        if (bounds == null) {
            return tiles;
        }

        return tiles.stream()
                    .filter(tile -> tile.bounds().intersects(bounds))
                    .toList();
    }

    private Double percentile(List<Double> values, double quantile) {
        if (values.size() == 1) {
            return values.getFirst();
        }
        double index = quantile * (values.size() - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        if (lowerIndex == upperIndex) {
            return values.get(lowerIndex);
        }
        double lowerValue = values.get(lowerIndex);
        double upperValue = values.get(upperIndex);
        double fraction = index - lowerIndex;
        return lowerValue + (upperValue - lowerValue) * fraction;
    }

    private List<Double> buildQuantiles(List<Double> values, int quantileCount) {
        return java.util.stream.IntStream.rangeClosed(1, quantileCount)
                                         .mapToObj(index -> percentile(values, (double) index / quantileCount))
                                         .toList();
    }
}