package net.privactivity.store.config;

import net.privactivity.store.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;



import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("inttest")
class AdminImportSecurityTest extends AbstractIntegrationTest {


    @Value("${local.server.port}")
    private int port;


    @Test
    void importAllRequiresCsrfForBasicAuth() {
        ResponseEntity<String> response = postWithEmptyBody("/api/admin/import-all", authenticatedHeaders());

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void importAllAllowsCsrfProtectedBasicAuth() {
        String csrfToken = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD);
        headers.add(HttpHeaders.COOKIE, "XSRF-TOKEN=" + csrfToken);
        headers.add("X-XSRF-TOKEN", csrfToken);

        ResponseEntity<String> response = postWithEmptyBody("/api/admin/import-all?removeAllBeforeImport=true",
                                                            headers);

        assertThat(response.getStatusCode().value())
                .as("body=%s", response.getBody())
                .isEqualTo(200);
    }

    private HttpHeaders authenticatedHeaders() {
        return basicAuthHeaders();
    }

    private ResponseEntity<String> postWithEmptyBody(String path, HttpHeaders headers) {
        try {
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));

                headers.forEach((name, values) -> values.forEach(value -> request.header(name, value)));

                request.method("POST", HttpRequest.BodyPublishers.noBody());

                HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());

                HttpHeaders responseHeaders = new HttpHeaders();
                response.headers().map().forEach(responseHeaders::put);
                return new ResponseEntity<>(response.body(), responseHeaders,
                                            org.springframework.http.HttpStatusCode.valueOf(response.statusCode()));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
