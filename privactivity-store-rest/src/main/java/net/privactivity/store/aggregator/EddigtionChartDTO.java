package net.privactivity.store.aggregator;

import net.privactivity.store.usecase.eddingtion.ComputeEddingtonChartUseCase;

import java.util.List;

public record EddigtionChartDTO(List<Integer> countsPerKM, int eddigtionNumber) {
    public EddigtionChartDTO(ComputeEddingtonChartUseCase.EddigtionChart eddigtionChart) {
        this(eddigtionChart.countsPerKM(), eddigtionChart.eddigtionNumber());
    }
}
