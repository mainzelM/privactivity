package net.privactivity.store.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class PrivactivityUserDetailsManager extends InMemoryUserDetailsManager {

    private final PasswordEncoder passwordEncoder;
    private final String usersFilePath;
    private final String initialAdminPasswordFilePath;
    private final ObjectMapper objectMapper;

    public PrivactivityUserDetailsManager(
            PasswordEncoder passwordEncoder,
            @Value("${privactivity.users.file.path:users.json}") String usersFilePath,
            @Value("${privactivity.bootstrap.admin-password-file.path:admin-password.txt}") String initialAdminPasswordFilePath) {
        this.passwordEncoder = passwordEncoder;
        this.usersFilePath = usersFilePath;
        this.initialAdminPasswordFilePath = initialAdminPasswordFilePath;
        this.objectMapper = new ObjectMapper();
        loadUsersFromFile();
    }

    private void loadUsersFromFile() {
        try {
            Path filePath = Paths.get(usersFilePath);

            if (!Files.exists(filePath)) {
                createDefaultUsersFile(filePath);
            }

            String jsonContent = Files.readString(filePath);
            logger.info("Read user config from %s".formatted(filePath.toAbsolutePath()));
            List<UserConfig> userConfigs = objectMapper.readValue(jsonContent, new TypeReference<>() {
            });

            List<UserDetails> users = new ArrayList<>();
            for (UserConfig userConfig : userConfigs) {
                UserDetails user = User.withUsername(userConfig.username())
                                       .password(userConfig.password())
                                       .roles(userConfig.roles().toArray(new String[0]))
                                       .build();
                users.add(user);
            }

            users.forEach(this::createUser);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load users from file: " + usersFilePath, e);
        }
    }

    private void createDefaultUsersFile(Path filePath) throws IOException {
        String adminPassword = readInitialAdminPassword();

        List<UserConfig> defaultUsers = List.of(
                new UserConfig("admin", passwordEncoder.encode(adminPassword), List.of("USER", "ADMIN")));

        String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(defaultUsers);
        Path parent = filePath.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("Null parent for " + filePath);
        }
        Files.createDirectories(parent);
        Files.writeString(filePath, jsonContent);
    }

    private String readInitialAdminPassword() throws IOException {
        Path adminPasswordFilePath = Paths.get(initialAdminPasswordFilePath);
        if (!Files.exists(adminPasswordFilePath) || !Files.isReadable(adminPasswordFilePath)) {
            throw new IllegalStateException(
                    "Cannot bootstrap admin user: configured admin password file does not exist or is not readable: "
                    + adminPasswordFilePath.toAbsolutePath());
        }

        String adminPassword = Files.readString(adminPasswordFilePath).trim();
        if (adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Cannot bootstrap admin user: configured admin password file is empty: "
                    + adminPasswordFilePath.toAbsolutePath());
        }

        return adminPassword;
    }

    public boolean changeUserPassword(String username, String currentPassword, String newPassword) {
        try {
            UserDetails user = loadUserByUsername(username);
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return false;
            }

            changePassword(currentPassword, passwordEncoder.encode(newPassword));

            updatePasswordInFile(username, newPassword);

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to change password for user: " + username, e);
        }
    }

    private void updatePasswordInFile(String username, String newPassword) throws IOException {
        Path filePath = Paths.get(usersFilePath);

        // Read current file
        String jsonContent = Files.readString(filePath);
        List<UserConfig> existingConfigs = objectMapper.readValue(jsonContent, new TypeReference<>() {
        });

        // Update the password for the specified user - store encrypted password in JSON
        List<UserConfig> updatedConfigs = existingConfigs.stream()
                                                         .map(config -> config.username().equals(username)
                                                                        ? new UserConfig(config.username(),
                                                                                         passwordEncoder.encode(newPassword), config.roles())
                                                                        : config)
                                                         .toList();

        // Write back to file
        String updatedJsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(updatedConfigs);
        Files.writeString(filePath, updatedJsonContent);
    }

    public record UserConfig(String username, String password, List<String> roles) {
    }
}