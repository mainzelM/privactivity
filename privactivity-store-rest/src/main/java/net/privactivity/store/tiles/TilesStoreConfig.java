package net.privactivity.store.tiles;

import net.privactivity.store.usecase.rebuildtiles.TilesProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TilesStoreConfig {

    @Bean
    TilesProperties tilesProperties(@Value("${privactivity.tiles.tile-width-degrees}") double tileWidthDegrees,
                                    @Value("${privactivity.tiles.tile-height-degrees}") double tileHeightDegrees) {
        return new TilesProperties(tileWidthDegrees, tileHeightDegrees);
    }
}