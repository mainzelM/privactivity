package net.privactivity.store.usecase.export.exportascsv;

public record CsvExportConfig(boolean latLon, boolean speed, boolean power, boolean heartRate, boolean cadence,
                              boolean altitude, boolean lap, int secondsToAverage) {
}
