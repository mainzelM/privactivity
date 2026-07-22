package net.privactivity.store.spring;

import net.privactivity.export.gpx.GpxExporterImpl;
import net.privactivity.store.usecase.export.adapter.GpxExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExporterConfig {
    @Bean
    public GpxExporter gpxExporter() {
        return new GpxExporterImpl();
    }
}
