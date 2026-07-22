package net.privactivity.store.spring;

import net.privactivity.store.usecase.townfinder.adapter.TownFinder;
import net.privactivity.townfinder.adapter.geotools.StandardTownFinders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TownFinderConfig {

    @Bean
    public TownFinder townFinder() {
        return StandardTownFinders.combinedTownFinder();
    }
}
