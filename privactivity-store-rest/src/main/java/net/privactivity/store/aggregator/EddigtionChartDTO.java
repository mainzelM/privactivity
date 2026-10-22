package net.privactivity.store.aggregator;

import net.privactivity.store.usecase.eddingtion.ComputeEddingtonChartUseCase;

import java.util.List;

public record EddigtionChartDTO(List<Integer> countsPerKM, int eddigtionNumber, int missingToNext) {
    public EddigtionChartDTO(ComputeEddingtonChartUseCase.EddingtonChart eddingtonChart) {
        this(eddingtonChart.countsPerKM(), eddingtonChart.eddigtionNumber(), eddingtonChart.missingToNext());
    }
}
