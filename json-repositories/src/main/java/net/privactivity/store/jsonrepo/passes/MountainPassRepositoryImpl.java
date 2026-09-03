package net.privactivity.store.jsonrepo.passes;

import net.privactivity.domain.LatLon;
import net.privactivity.store.adapter.MountainPassRepository;
import net.privactivity.store.usecase.crossedpasses.MountainPass;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MountainPassRepositoryImpl implements MountainPassRepository {

    private static final String CSV_RESOURCE_PATTERN = "classpath:passes/passes-*.csv";
    private static final String FILENAME_PREFIX = "passes-";
    private static final String FILENAME_SUFFIX = ".csv";

    @Override
    public List<MountainPass> getAllPasses() {
        List<MountainPass> passes = new ArrayList<>();
        Resource[] resources;
        try {
            resources = new PathMatchingResourcePatternResolver().getResources(CSV_RESOURCE_PATTERN);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list resources matching " + CSV_RESOURCE_PATTERN, e);
        }
        for (Resource resource : resources) {
            passes.addAll(readPasses(resource));
        }
        return passes;
    }

    private List<MountainPass> readPasses(Resource resource) {
        String country = countryFromFilename(resource.getFilename());
        List<MountainPass> passes = new ArrayList<>();
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                passes.add(parseLine(line, country));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + resource.getFilename(), e);
        }
        return passes;
    }

    private String countryFromFilename(String filename) {
        if (filename == null || !filename.startsWith(FILENAME_PREFIX) || !filename.endsWith(FILENAME_SUFFIX)) {
            throw new IllegalStateException("Unexpected passes CSV filename: " + filename);
        }
        String countryCode = filename.substring(FILENAME_PREFIX.length(), filename.length() - FILENAME_SUFFIX.length());
        return countryCode.toUpperCase(Locale.ROOT);
    }

    private MountainPass parseLine(String line, String country) {
        String[] columns = line.split(";");
        String name = unquote(columns[0]);
        double lat = Double.parseDouble(unquote(columns[2]));
        double lon = Double.parseDouble(unquote(columns[3]));
        return new MountainPass(name, country, new LatLon(lat, lon));
    }

    private String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
