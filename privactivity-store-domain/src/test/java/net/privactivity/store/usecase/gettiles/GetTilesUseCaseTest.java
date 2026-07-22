package net.privactivity.store.usecase.gettiles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;



import static org.assertj.core.api.Assertions.assertThat;

class GetTilesUseCaseTest {

    private GetTilesUseCase testee;

    @BeforeEach
    void setUp() {
        testee = new GetTilesUseCase(null);
    }


    @Test
    void legendFor_shouldCompute20QuantilesForSelectedMetric() {
        List<TileSummary> tiles = List.of(
                new TileSummary("a", new TileBounds(0, 1, 0, 1), null, null, 100, 0, 10.0, null, List.of(1L)),
                new TileSummary("b", new TileBounds(0, 1, 1, 2), null, null, 200, 0, 20.0, null, List.of(2L, 3L)),
                new TileSummary("c", new TileBounds(1, 2, 0, 1), null, null, 300, 0, 30.0, null, List.of(4L, 5L, 6L)),
                new TileSummary("d", new TileBounds(1, 2, 1, 2), null, null, 400, 0, 40.0, null, List.of(7L, 8L, 9L, 10L)));

        TileLegend result = testee.legendFor(tiles, TileMetric.AVERAGE_POWER);

        assertThat(result.min()).isEqualTo(10.0);
        assertThat(result.max()).isEqualTo(40.0);
        assertThat(result.quantiles()).hasSize(20);
        assertThat(result.quantiles().get(4)).isEqualTo(17.5); // Q5 = 25%
        assertThat(result.quantiles().get(9)).isEqualTo(25.0); // Q10 = 50%
        assertThat(result.quantiles().get(14)).isEqualTo(32.5); // Q15 = 75%
        assertThat(result.quantiles().get(19)).isEqualTo(40.0); // Q20 = 100%
    }

    @Test
    void tileSummary_shouldDefaultMissingActivityIdsToEmptyList() {
        TileSummary testee = new TileSummary("a", new TileBounds(0, 1, 0, 1), null, null, 100, 0, 10.0, null, null);

        assertThat(testee.activityIds()).isEmpty();
    }
}