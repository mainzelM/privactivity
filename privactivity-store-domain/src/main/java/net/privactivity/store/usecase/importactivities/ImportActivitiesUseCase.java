package net.privactivity.store.usecase.importactivities;

import net.privactivity.domain.Activity;
import net.privactivity.store.adapter.ActivityRepository;
import net.privactivity.store.usecase.importactivities.adapter.ActivitiesJsonReader;
import net.privactivity.store.usecase.importactivities.adapter.FitDecoder;
import net.privactivity.store.usecase.importactivities.adapter.GarminActivity;
import net.privactivity.store.usecase.importactivities.adapter.GpxImporter;
import net.privactivity.store.usecase.importactivities.adapter.TcxImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


public class ImportActivitiesUseCase {

    private final ActivityRepository activityRepository;
    private final FitDecoder fitDecoder;
    private final GpxImporter gpxImporter;
    private final TcxImporter tcxImporter;
    private final ActivitiesJsonReader activitiesJsonReader;

    private static final Logger logger = LoggerFactory.getLogger(ImportActivitiesUseCase.class);
    private final String ID_REGEXP = "(\\d+)_\\w+\\.\\w+";
    private final Pattern ID_PATTERN = Pattern.compile(ID_REGEXP);
    private static final DateTimeFormatter FIT_FILENAME_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd-HH-mm-ss");


    public ImportActivitiesUseCase(ActivityRepository activityRepository, FitDecoder fitDecoder,
                                   GpxImporter gpxImporter,
                                   TcxImporter tcxImporter, ActivitiesJsonReader activitiesJsonReader) {
        this.activityRepository = activityRepository;
        this.fitDecoder = fitDecoder;
        this.gpxImporter = gpxImporter;
        this.tcxImporter = tcxImporter;
        this.activitiesJsonReader = activitiesJsonReader;
    }

    public void importDirectory(Path dir, boolean removeAllBeforeImport) {
        try {
            if (removeAllBeforeImport) {
                removeAll();
            }
            importZipFiles(dir);
            importPlainFitFiles(dir);
            activityRepository.gc();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void importPlainFitFiles(Path dir) throws IOException {
        try (Stream<Path> stream = Files.find(dir, 1, (p, _) -> p.toString().endsWith(".fit"))) {
            stream.forEach(this::importFitFile);
        }
    }

    private void importZipFiles(Path dir) throws IOException {
        List<GarminActivity> activities = activitiesJsonReader.read(dir);
        try (Stream<Path> stream = Files.find(dir, 1, (p, _) -> p.toString().endsWith(".zip"))) {
            stream.map(this::importZip)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .forEach(id -> addTitleFromActivitiesJson(id, activities));
        }
    }


    private void addTitleFromActivitiesJson(long id, List<GarminActivity> jsonActivities) {
        if (id < 0) {
            System.err.println("skippiing add title for " + id);
            return;
        }
        jsonActivities.stream()
                      .filter(act -> act.getActivityId() == id)
                      .findFirst()
                      .ifPresent(act -> updateActivityFromJson(id, act));
    }

    private void updateActivityFromJson(long id, GarminActivity garminActivity) {
        Activity activity = activityRepository.getActivityById(id, false);
        activity = activity.withTitle(garminActivity.getActivityName());
        if (!activity.totals().movingTime().isPresent() && garminActivity.getMovingDuration() > 0) {
            activity = activity.withMovingTime(garminActivity.getMovingDuration());
        }
        if (!activity.totals().distanceInMeters().isPresent() && garminActivity.getDistance() > 0) {
            activity = activity.withDistance(garminActivity.getDistance());
        }

        if (!activity.averages().heartRate().isPresent() && garminActivity.getAverageHR() > 0) {
            activity = activity.withAverageHeartRate((int) garminActivity.getAverageHR());
        }

        if (!activity.maxima().heartRate().isPresent() && garminActivity.getMaxHR() > 0) {
            activity = activity.withMaximumHeartRate((int) garminActivity.getMaxHR());
        }

        if (!activity.averages().speedInMetersPerHour().isPresent() && garminActivity.getAverageSpeed() > 0) {
            double avgSpeed = garminActivity.getDistance() / garminActivity.getMovingDuration() * 3600;
            activity = activity.withAverageSpeed((int) avgSpeed);
        }

        if (!activity.maxima().speedInMetersPerHour().isPresent() && garminActivity.getMaxSpeed() > 0) {
            activity = activity.withMaximumSpeed((int) (garminActivity.getMaxSpeed() * 1000));
        }

        if (!activity.averages().power().isPresent() && garminActivity.getAvgPower() > 0) {
            activity = activity.withAveragePower((int) garminActivity.getAvgPower());
        }

        if (!activity.maxima().power().isPresent() && garminActivity.getMaxPower() > 0) {
            activity = activity.withMaximumPower((int) garminActivity.getMaxPower());
        }

        if (!activity.totals().ascent().isPresent() && garminActivity.getElevationGain() > 0) {
            activity = activity.withAscent((int) garminActivity.getElevationGain());
        }

        if (!activity.totals().descent().isPresent() && garminActivity.getElevationLoss() > 0) {
            activity = activity.withDescent((int) garminActivity.getElevationLoss());
        }

        if (!activity.maxima().altitude().isPresent() && garminActivity.getMaxElevation() > 0) {
            activity = activity.withMaxAltitutde((int) garminActivity.getMaxElevation());
        }

        activityRepository.updateActivity(activity, false);
    }

    public Optional<Long> importZip(Path path) {
        try {
            Optional<Activity> activity = decodeZip(path, activityRepository::containsActivityId);
            if (activity.isPresent()) {
                activityRepository.addActivity(activity.get());
                return Optional.of(activity.get().id());
            } else {
                return Optional.empty();
            }
        } catch (ZipException e) {
            logger.info("While importing {} {}", path, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("While importing {}", path, e);
            return Optional.empty();
        }
    }

    public void importFitFile(Path path) {
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            long id = parseFilenameAsId(path.getFileName().toString());
            if (!activityRepository.containsActivityId(id)) {
                Activity activity = fitDecoder.extractFit(fis, id);
                activityRepository.addActivity(activity);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long parseFilenameAsId(String fitFile) {
        String timestamp = fitFile.endsWith(".fit") ? fitFile.substring(0, fitFile.length() - 4) : fitFile;
        return LocalDateTime.parse(timestamp, FIT_FILENAME_TIMESTAMP_FORMAT)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli();
    }


    public Optional<Activity> decodeZip(Path fileZip, Predicate<Long> skipId) throws IOException {
        try (ZipFile zf = new ZipFile(fileZip.toFile())) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            if (!entries.hasMoreElements()) {
                throw new ZipException("No entry in zip " + fileZip);
            }
            ZipEntry zipEntry = entries.nextElement();

            String name = zipEntry.getName();
            long id = extractId(name);
            if (skipId.test(id)) {
                return Optional.empty();
            } else {
                return Optional.of(asActivity(zf, zipEntry, name, id));
            }
        }
    }

    private Activity asActivity(ZipFile zf, ZipEntry zipEntry, String name, long id) throws IOException {
        try (InputStream is = zf.getInputStream(zipEntry)) {
            if (name.endsWith(".fit")) {
                return fitDecoder.extractFit(is, id);
            } else if (name.endsWith(".gpx")) {
                return gpxImporter.importGpxAsActivity(is, id);
            } else if (name.endsWith(".tcx")) {
                return tcxImporter.importTcx(is, id);
            } else {
                throw new RuntimeException("Found unknown entry " + zipEntry);
            }
        }
    }

    private long extractId(String zipEntryName) {
        Matcher matcher = ID_PATTERN.matcher(zipEntryName);
        if (matcher.find()) {
            String id = matcher.group(1);
            return Long.parseLong(id);
        } else {
            throw new RuntimeException("Cannot extract activity ID from " + zipEntryName);
        }
    }

    private void removeAll() {
        activityRepository.removeAll();
    }

    static void main(String[] args) throws IOException {
  /*      FitDecoder fitDecoder = new FitDecoderImpl();
        FitImporter fitImporter = new FitImporter(null, fitDecoder);
        Optional<Activity> activity = fitImporter.decodeZip(Path.of(args[0]), aLong -> false);
        System.out.println(activity.orElseThrow());*/
    }
}
