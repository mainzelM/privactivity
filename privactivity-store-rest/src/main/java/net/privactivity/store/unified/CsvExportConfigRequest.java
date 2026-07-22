package net.privactivity.store.unified;

import net.privactivity.store.usecase.export.exportascsv.CsvExportConfig;

public record CsvExportConfigRequest(Boolean latLon,
                                     Boolean speed,
                                     Boolean power,
                                     Boolean heartRate,
                                     Boolean cadence,
                                     Boolean altitude,
                                     Boolean lap,
                                     Integer secondsToAverage) {

    private static final boolean DEFAULT_LAT_LON = false;
    private static final boolean DEFAULT_SPEED = false;
    private static final boolean DEFAULT_POWER = true;
    private static final boolean DEFAULT_HEART_RATE = true;
    private static final boolean DEFAULT_CADENCE = false;
    private static final boolean DEFAULT_ALTITUDE = false;
    private static final boolean DEFAULT_LAP = false;
    private static final int DEFAULT_SECONDS_TO_AVERAGE = 60;

    public static CsvExportConfigRequest defaults() {
        return new CsvExportConfigRequest(
                DEFAULT_LAT_LON,
                DEFAULT_SPEED,
                DEFAULT_POWER,
                DEFAULT_HEART_RATE,
                DEFAULT_CADENCE,
                DEFAULT_ALTITUDE,
                DEFAULT_LAP,
                DEFAULT_SECONDS_TO_AVERAGE
        );
    }

    public CsvExportConfig toCsvExporterConfig() {
        int effectiveSecondsToAverage = secondsToAverage != null && secondsToAverage > 0
                                        ? secondsToAverage
                                        : DEFAULT_SECONDS_TO_AVERAGE;
        return new CsvExportConfig(
                latLon != null ? latLon : DEFAULT_LAT_LON,
                speed != null ? speed : DEFAULT_SPEED,
                power != null ? power : DEFAULT_POWER,
                heartRate != null ? heartRate : DEFAULT_HEART_RATE,
                cadence != null ? cadence : DEFAULT_CADENCE,
                altitude != null ? altitude : DEFAULT_ALTITUDE,
                lap != null ? lap : DEFAULT_LAP,
                effectiveSecondsToAverage
        );
    }
}