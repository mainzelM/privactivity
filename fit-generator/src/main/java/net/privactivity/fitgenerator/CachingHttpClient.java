package net.privactivity.fitgenerator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

/**
 * GET client for the public OSM services. Successful responses are cached on disk by URL, failed
 * requests are retried and requests are spaced out to respect the fair-use policies.
 */
public class CachingHttpClient {

    private static final String USER_AGENT = "privactivity-fit-generator/1.0 (test data generator)";
    private static final int MAX_ATTEMPTS = 6;

    private final HttpClient httpClient = HttpClient.newBuilder()
                                                    .connectTimeout(Duration.ofSeconds(20))
                                                    .followRedirects(HttpClient.Redirect.NORMAL)
                                                    .build();
    private final Path cacheDir;
    private final long minRequestIntervalMillis;
    private long lastRequestMillis;

    public CachingHttpClient(Path cacheDir, long minRequestIntervalMillis) {
        this.cacheDir = cacheDir;
        this.minRequestIntervalMillis = minRequestIntervalMillis;
    }

    public String get(String url) {
        Path cacheFile = cacheDir.resolve(sha256(url) + ".json");
        try {
            if (Files.exists(cacheFile)) {
                return Files.readString(cacheFile, StandardCharsets.UTF_8);
            }
            String body = fetch(url);
            Files.createDirectories(cacheDir);
            Files.writeString(cacheFile, body, StandardCharsets.UTF_8);
            return body;
        } catch (IOException e) {
            throw new GenerationException("Cannot access cache file " + cacheFile, e);
        }
    }

    private String fetch(String url) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                                         .timeout(Duration.ofSeconds(90))
                                         .header("User-Agent", USER_AGENT)
                                         .GET()
                                         .build();
        GenerationException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            throttle();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return response.body();
                }
                last = new GenerationException("HTTP " + response.statusCode() + " from " + url);
                if (response.statusCode() == 429) {
                    long waitSeconds = response.headers().firstValue("Retry-After").map(this::parseSeconds)
                                               .orElse(20L * attempt);
                    System.err.println("Rate limited, waiting " + waitSeconds + " s before retrying");
                    sleep(waitSeconds * 1000);
                    continue;
                }
            } catch (IOException e) {
                last = new GenerationException("Request failed: " + url + " (" + e + ")", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GenerationException("Interrupted while requesting " + url, e);
            }
            sleep(2000L * attempt);
        }
        throw last;
    }

    private long parseSeconds(String value) {
        try {
            return Math.clamp(Long.parseLong(value.trim()), 1L, 120L);
        } catch (NumberFormatException e) {
            return 20;
        }
    }

    private void throttle() {
        long wait = lastRequestMillis + minRequestIntervalMillis - System.currentTimeMillis();
        if (wait > 0) {
            sleep(wait);
        }
        lastRequestMillis = System.currentTimeMillis();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GenerationException("Interrupted", e);
        }
    }

    private String sha256(String text) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
