package net.privactivity.store.jsonrepo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class JsonRepository<T> {
    private static final Logger logger = LoggerFactory.getLogger(JsonRepository.class);

    protected final ObjectMapper objectMapper;
    protected final Path jsonFile;
    protected volatile T cache;

    public JsonRepository(String fileName, String storageDirectoryStr) {
        this.objectMapper = createObjectMapper();
        try {
            Path storageDirectory = resolveWritableStorageDirectory(storageDirectoryStr);
            this.jsonFile = storageDirectory.resolve(fileName);
            if (!Files.exists(jsonFile)) {
                writeSnapshot(makeEmptySnapshot());
            }
            this.cache = loadSnapshot();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize JSON repository for " + fileName, e);
        }
    }

    protected abstract T makeEmptySnapshot();

    protected abstract TypeReference<T> snapshotType();

    private T loadSnapshot() {
        try {
            return objectMapper.readValue(jsonFile.toFile(), snapshotType());
        } catch (IOException e) {
            logger.warn("Failed to load JSON snapshot from {}. Returning empty snapshot without creating file.",
                        jsonFile, e);
            return makeEmptySnapshot();
        }
    }

    private void writeSnapshot(T snapshot) throws IOException {
        Path tempFile = jsonFile.resolveSibling(jsonFile.getFileName() + ".tmp");
        objectMapper.writeValue(tempFile.toFile(), snapshot);
        try {
            Files.move(tempFile, jsonFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException atomicMoveFailure) {
            Files.move(tempFile, jsonFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    protected ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    protected Path resolveWritableStorageDirectory(String configuredDirectory) throws IOException {
        Path configuredPath = Path.of(configuredDirectory);
        try {
            Files.createDirectories(configuredPath);
            return configuredPath;
        } catch (IOException e) {
            Path fallback = Path.of(System.getProperty("java.io.tmpdir"), "privactivity", "json-repositories");
            logger.warn("Configured JSON storage directory '{}' is not writable. Falling back to" +
                        " '{}'.",
                        configuredDirectory, fallback, e);
            Files.createDirectories(fallback);
            return fallback;
        }
    }


    public synchronized void replaceSnapshot(T snapshot) {
        try {
            writeSnapshot(snapshot);
            this.cache = snapshot;
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist JSON snapshot to " + jsonFile, e);
        }
    }
}
