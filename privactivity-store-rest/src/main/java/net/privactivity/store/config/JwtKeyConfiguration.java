package net.privactivity.store.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@Configuration
public class JwtKeyConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyConfiguration.class);

    @Bean
    @Profile("dev")
    public KeyPair devJwtKeyPair(@Value("${jwt.dev.private-key-path:${HOME}/tmp/privactivity/jwt/dev/app.key}") String privateKeyPath,
                                 @Value("${jwt.dev.public-key-path:${HOME}/tmp/privactivity/jwt/dev/app.pub}") String publicKeyPath) {
        Path privatePath = asPath(privateKeyPath, "jwt.dev.private-key-path");
        Path publicPath = asPath(publicKeyPath, "jwt.dev.public-key-path");

        if (Files.isRegularFile(privatePath) && Files.isReadable(privatePath)
            && Files.isRegularFile(publicPath) && Files.isReadable(publicPath)) {
            RSAPublicKey publicKey = readKey(publicPath, "jwt.dev.public-key-path", RsaKeyConverters.x509());
            RSAPrivateKey privateKey = readKey(privatePath, "jwt.dev.private-key-path", RsaKeyConverters.pkcs8());
            return new KeyPair(publicKey, privateKey);
        }

        KeyPair keyPair = generateKeyPair();
        persistDevKeyPair(keyPair, privatePath, publicPath);
        return keyPair;
    }

    @Bean
    @Profile("inttest")
    public KeyPair inttestJwtKeyPair() {
        return generateKeyPair();
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot initialize RSA key pair generator.", e);
        }
    }

    @Bean
    @Profile("docker")
    public KeyPair dockerJwtKeyPair(@Value("${jwt.public.key}") Resource publicKeyResource,
                                    @Value("${jwt.private.key}") Resource privateKeyResource) {
        try {
            log.info("Loading JWT public key from {}", publicKeyResource);
            RSAPublicKey publicKey = readKey(publicKeyResource, "jwt.public.key", RsaKeyConverters.x509());
            RSAPrivateKey privateKey = readKey(privateKeyResource, "jwt.private.key", RsaKeyConverters.pkcs8());
            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize RSA key pair generator.", e);
        }
    }

    @Bean
    public RSAPublicKey jwtPublicKey(KeyPair jwtKeyPair) {
        return (RSAPublicKey) jwtKeyPair.getPublic();
    }

    @Bean
    public RSAPrivateKey jwtPrivateKey(KeyPair jwtKeyPair) {
        return (RSAPrivateKey) jwtKeyPair.getPrivate();
    }

    private <T> T readKey(Resource resource, String propertyName, Converter<InputStream, T> converter) {
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("Configured resource for " + propertyName
                                            + " does not exist or is not readable: " + resource);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            T key = converter.convert(inputStream);
            if (key == null) {
                throw new IllegalStateException("Parsed key for " + propertyName + " is null: " + resource);
            }
            return key;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read key resource for " + propertyName + ": " + resource, e);
        }
    }

    private <T> T readKey(Path path, String propertyName, Converter<InputStream, T> converter) {
        if (!Files.exists(path) || !Files.isReadable(path)) {
            throw new IllegalStateException("Configured file for " + propertyName
                                            + " does not exist or is not readable: " + path.toAbsolutePath());
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            T key = converter.convert(inputStream);
            if (key == null) {
                throw new IllegalStateException("Parsed key for " + propertyName + " is null: " + path.toAbsolutePath());
            }
            return key;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read key file for " + propertyName + ": " + path.toAbsolutePath(), e);
        }
    }

    private void persistDevKeyPair(KeyPair keyPair, Path privatePath, Path publicPath) {
        try {
            createParentDirectories(privatePath);
            createParentDirectories(publicPath);

            writePem(privatePath, "PRIVATE KEY", keyPair.getPrivate().getEncoded());
            writePem(publicPath, "PUBLIC KEY", keyPair.getPublic().getEncoded());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to persist generated dev JWT key pair.", e);
        }
    }

    private void createParentDirectories(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private void writePem(Path path, String label, byte[] keyBytes) throws IOException {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(keyBytes);
        String pem = "-----BEGIN " + label + "-----\n"
                     + base64 + "\n"
                     + "-----END " + label + "-----\n";
        Files.writeString(path, pem, StandardCharsets.US_ASCII);
    }

    private Path asPath(String path, String propertyName) {
        if (path == null || path.isBlank()) {
            throw new IllegalStateException(propertyName + " must not be blank.");
        }
        return Paths.get(path);
    }
}
