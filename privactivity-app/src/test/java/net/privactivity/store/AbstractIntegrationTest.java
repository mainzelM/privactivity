package net.privactivity.store;

import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractIntegrationTest {

    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "secret";


    private static Path createRootDirectory() {
        try {
            Path root = Files.createTempDirectory("privactivity-integration-test");
            Files.createDirectories(root.resolve("import"));
            Files.createDirectories(root.resolve("climb"));
            Files.createDirectories(root.resolve("fit"));
            Files.createDirectories(root.resolve("metadata"));
            Files.createDirectories(root.resolve("json"));
            return root;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Path createUsersFile(Path rootDirectory) {
        try {
            Path file = rootDirectory.resolve("users.json");
            Files.createDirectories(file.getParent());
            String passwordHash = new BCryptPasswordEncoder().encode(ADMIN_PASSWORD);
            Files.writeString(file, """
                    [
                      {
                        "username": "%s",
                        "password": "%s",
                        "roles": ["USER", "ADMIN"]
                      }
                    ]
                    """.formatted(ADMIN_USERNAME, passwordHash));
            return file;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        final Path ROOT_DIRECTORY = createRootDirectory();
        final Path USERS_FILE = createUsersFile(ROOT_DIRECTORY);
        registry.add("privactivity.users.file.path", USERS_FILE::toString);
        registry.add("privactivity.store.climb.storage-directory", () -> ROOT_DIRECTORY.resolve("climb").toString());
        registry.add("privactivity.store.repo.activity.storage-directory",
                     () -> ROOT_DIRECTORY.resolve("fit").toString());
        registry.add("privactivity.store.metadata.storage-directory",
                     () -> ROOT_DIRECTORY.resolve("metadata").toString());
        registry.add("privactivity.store.json.storage-directory", () -> ROOT_DIRECTORY.resolve("json").toString());
        registry.add("privactivity.fit.import.directory", () -> ROOT_DIRECTORY.resolve("import").toString());
    }

    protected static HttpHeaders basicAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD);
        return headers;
    }
}
