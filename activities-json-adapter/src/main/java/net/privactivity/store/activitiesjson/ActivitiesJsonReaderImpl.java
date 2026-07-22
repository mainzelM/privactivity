package net.privactivity.store.activitiesjson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import net.privactivity.store.usecase.importactivities.adapter.ActivitiesJsonReader;
import net.privactivity.store.usecase.importactivities.adapter.GarminActivity;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class ActivitiesJsonReaderImpl implements ActivitiesJsonReader {
    private static final String ACTIVITIES_1_to_X_PREFIX = "activities-1-";
    private static final String ACTIVITIES_1000_to_X_PREFIX = "activities-1000-";
    private static final String ACTIVITIES_SUFFIX = ".json";

    private final ObjectMapper objectMapper;

    public ActivitiesJsonReaderImpl() {
        objectMapper = JsonMapper.builder()
                                 .findAndAddModules()
                                 .build();
    }

    private Optional<Path> findLatestActivity(Path dir, String activitiesPrefix) {
        try {
            if (!Files.exists(dir)) {
                throw new RuntimeException("Path to activities does not exist " + dir);
            }
            OptionalInt max = Files.list(dir)
                                   .map(Path::getFileName)
                                   .map(Path::toString)
                                   .filter(p -> p.startsWith(activitiesPrefix))
                                   .filter(p -> p.endsWith(ACTIVITIES_SUFFIX))
                                   .map(p -> p.replace(activitiesPrefix, ""))
                                   .map(p -> p.replace(ACTIVITIES_SUFFIX, ""))
                                   .mapToInt(Integer::valueOf)
                                   .max();
            if (max.isPresent()) {
                return Optional.of(dir.resolve(activitiesPrefix + max.orElseThrow() + ACTIVITIES_SUFFIX));
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<GarminActivity> read(Path dir) {
        List<GarminActivity> activities = new ArrayList<>();
        activities.addAll(readPrefixed(dir, ACTIVITIES_1_to_X_PREFIX));
        activities.addAll(readPrefixed(dir, ACTIVITIES_1000_to_X_PREFIX));
        return activities;
    }

    private List<GarminActivityImpl> readPrefixed(Path dir, String activitiesPrefix) {
        try {
            Optional<Path> latestActivityPath = findLatestActivity(dir, activitiesPrefix);
            if (latestActivityPath.isPresent()) {
                return readFromStream(new FileInputStream(latestActivityPath.orElseThrow().toFile()));
            } else {
                return Collections.emptyList();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    List<GarminActivityImpl> readFromStream(InputStream inputStream) {
        try {
            return objectMapper.readValue(inputStream,
                                          new TypeReference<>() {
                                          });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
