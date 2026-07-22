package net.privactivity.store.config;

import net.privactivity.store.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;



import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("inttest")
class AuthLoginSecurityTest extends AbstractIntegrationTest {

    private final HttpClient client = HttpClient.newBuilder()
                                                .cookieHandler(new CookieManager())
                                                .build();

    @Value("${local.server.port}")
    private int port;


    @Test
    void loginSucceedsWithCsrfTokenOnFirstAttempt() {
        ResponseEntity<String> csrfResponse = execute("GET", "/api/auth/csrf", basicHeaders());

        assertThat(csrfResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(csrfResponse.getBody()).isNotBlank();

        HttpHeaders headers = basicHeaders();
        headers.add("X-XSRF-TOKEN", csrfResponse.getBody());

        ResponseEntity<String> loginResponse = execute("POST", "/api/auth", headers);

        assertThat(loginResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(loginResponse.getBody()).isNotBlank();
    }

    @Test
    void loginStillRejectsMissingCsrfToken() {
        ResponseEntity<String> loginResponse = execute("POST", "/api/auth", basicHeaders());

        assertThat(loginResponse.getStatusCode().value()).isEqualTo(403);
    }

    private HttpHeaders basicHeaders() {
        return basicAuthHeaders();
    }

    private ResponseEntity<String> execute(String method, String path, HttpHeaders headers) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
            headers.forEach((name, values) -> values.forEach(value -> request.header(name, value)));

            request.method(method, HttpRequest.BodyPublishers.noBody());

            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            HttpHeaders responseHeaders = new HttpHeaders();
            response.headers().map().forEach(responseHeaders::put);
            return new ResponseEntity<>(response.body(), responseHeaders,
                                        org.springframework.http.HttpStatusCode.valueOf(response.statusCode()));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException(e);
        }
    }

}
