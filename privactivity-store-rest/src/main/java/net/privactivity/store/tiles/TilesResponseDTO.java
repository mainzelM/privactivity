package net.privactivity.store.tiles;

import com.fasterxml.jackson.annotation.JsonFormat;
import net.privactivity.store.usecase.gettiles.TileLegend;
import net.privactivity.store.usecase.gettiles.TileMetric;
import net.privactivity.store.usecase.gettiles.TileSummary;
import net.privactivity.store.usecase.gettiles.TilesResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public record TilesResponseDTO(
        TileMetric metric,
        TileLegend legend,
        double tileWidthDegrees,
        double tileHeightDegrees,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        ZonedDateTime generatedAt,
        List<TileSummary> tiles) {

    public TilesResponseDTO(TilesResponse response) {
        this(
                response.metric(),
                response.legend(),
                response.tileWidthDegrees(),
                response.tileHeightDegrees(),
                response.generatedAt().atZone(ZoneId.of("UTC")),
                response.tiles());
    }
}
