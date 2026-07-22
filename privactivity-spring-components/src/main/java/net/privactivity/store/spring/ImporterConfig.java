package net.privactivity.store.spring;

import net.privactivity.fit.decode.FitDecoderImpl;
import net.privactivity.gpx.GpxImporterImpl;
import net.privactivity.store.activitiesjson.ActivitiesJsonReaderImpl;
import net.privactivity.store.usecase.importactivities.adapter.ActivitiesJsonReader;
import net.privactivity.store.usecase.importactivities.adapter.FitDecoder;
import net.privactivity.store.usecase.importactivities.adapter.GpxImporter;
import net.privactivity.store.usecase.importactivities.adapter.TcxImporter;
import net.privactivity.tcx.TcxImporterImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImporterConfig {

    @Bean
    public FitDecoder fitDecoder() {
        return new FitDecoderImpl();
    }

    @Bean
    public GpxImporter gpxImporter() {
        return new GpxImporterImpl();
    }

    @Bean
    public TcxImporter tcxImporter() {
        return new TcxImporterImpl();
    }

    @Bean
    public ActivitiesJsonReader activitiesJsonReader() {
        return new ActivitiesJsonReaderImpl();
    }

}
