package net.privactivity.store.usecase.export.exportascsv;

import net.privactivity.domain.Activity;
import net.privactivity.domain.LatLon;
import net.privactivity.domain.Maybe;
import net.privactivity.domain.Waypoint;
import net.privactivity.store.adapter.ActivityRepository;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExportAsCsvUseCase {
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final ActivityRepository activityRepository;

    public ExportAsCsvUseCase(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public CsvExportResult exportActivityAsCsv(long trackId, CsvExportConfig csvExportConfig) {
        Activity activity = activityRepository.getActivityById(trackId, true);
        StringWriter writer = new StringWriter();

        // Write CSV header
        writer.write("Time");
        if (csvExportConfig.latLon()) {
            writer.write(",Lat,Lon");
        }
        if (csvExportConfig.speed()) {
            writer.write(",Speed");
        }
        if (csvExportConfig.power()) {
            writer.write(",Power");
        }
        if (csvExportConfig.heartRate()) {
            writer.write(",HeartRate");
        }
        if (csvExportConfig.cadence()) {
            writer.write(",Cadence");
        }
        if (csvExportConfig.altitude()) {
            writer.write(",Altitude");
        }
        if (csvExportConfig.lap()) {
            writer.write(",Lap");
        }
        writer.write("\n");

        // Collect waypoints in current window and process windows when they complete
        List<Waypoint> currentWindow = new ArrayList<>();
        int lastProcessedWindow = -1;

        for (Waypoint waypoint : activity.waypoints()) {
            int currentWindowIndex = waypoint.secondsSinceStart() / csvExportConfig.secondsToAverage();

            // If we're moving to a new window, process the previous window
            if (currentWindowIndex > lastProcessedWindow) {
                // Process any waypoints that were in the previous window
                if (!currentWindow.isEmpty()) {
                    writer.write(writeAveragedWaypoint(currentWindow, csvExportConfig));
                }

                // Start collecting waypoints for the new window
                currentWindow.clear();
                lastProcessedWindow = currentWindowIndex;
            }

            // Add the current waypoint to the window
            currentWindow.add(waypoint);
        }

        // Process any remaining waypoints in the last window
        if (!currentWindow.isEmpty()) {
            writer.write(writeAveragedWaypoint(currentWindow, csvExportConfig));
        }

        return new CsvExportResult(fileNameFor(activity), writer.toString());
    }

    private String writeAveragedWaypoint(List<Waypoint> windowWaypoints,
                                         CsvExportConfig csvExportConfig) {
        StringBuilder sb = new StringBuilder();

        // Use the time from first waypoint in window (as this is what we're grouping by)
        Waypoint firstWaypoint = windowWaypoints.getFirst();
        sb.append(firstWaypoint.secondsSinceStart());

        if (csvExportConfig.latLon()) {
            sb.append(",");
            Maybe<LatLon> latlon = firstWaypoint.latlon();
            if (latlon.isPresent()) {
                sb.append(latlon.orThrow().lat());
            }
            sb.append(",");
            if (latlon.isPresent()) {
                sb.append(latlon.orThrow().lon());
            }
        }

        if (csvExportConfig.speed()) {
            sb.append(",");
            int count = 0;
            long sum = 0;
            for (Waypoint wp : windowWaypoints) {
                if (wp.speedInMeterPerHour().isPresent()) {
                    sum += wp.speedInMeterPerHour().orThrow();
                    count++;
                }
            }
            if (count > 0) {
                sb.append(sum / count);
            }
        }

        if (csvExportConfig.power()) {
            sb.append(",");
            int count = 0;
            long sum = 0;
            for (Waypoint wp : windowWaypoints) {
                if (wp.power().isPresent()) {
                    sum += wp.power().orThrow();
                    count++;
                }
            }
            if (count > 0) {
                sb.append(sum / count);
            }
        }

        if (csvExportConfig.heartRate()) {
            sb.append(",");
            int count = 0;
            long sum = 0;
            for (Waypoint wp : windowWaypoints) {
                if (wp.heartRate().isPresent()) {
                    sum += wp.heartRate().orThrow();
                    count++;
                }
            }
            if (count > 0) {
                sb.append(sum / count);
            }
        }

        if (csvExportConfig.cadence()) {
            sb.append(",");
            int count = 0;
            long sum = 0;
            for (Waypoint wp : windowWaypoints) {
                if (wp.cadence().isPresent()) {
                    sum += wp.cadence().orThrow();
                    count++;
                }
            }
            if (count > 0) {
                sb.append(sum / count);
            }
        }

        if (csvExportConfig.altitude()) {
            sb.append(",");
            int count = 0;
            long sum = 0;
            for (Waypoint wp : windowWaypoints) {
                if (wp.altitude().isPresent()) {
                    sum += wp.altitude().orThrow();
                    count++;
                }
            }
            if (count > 0) {
                sb.append(sum / count);
            }
        }

        if (csvExportConfig.lap()) {
            sb.append(",");
            long sum = 0;
            for (Waypoint wp : windowWaypoints) {
                sum += wp.lap();
            }
            int avgLap = (int) Math.round((double) sum / windowWaypoints.size());
            sb.append(avgLap);
        }

        sb.append("\n");
        return sb.toString();
    }

    private String fileNameFor(Activity activity) {
        if (activity.start() == null) {
            return "activity-" + activity.id() + ".csv";
        }
        return "activity-" + activity.id() + "-" + activity.start().toLocalDate().format(FILE_DATE_FORMATTER) + ".csv";
    }

}
