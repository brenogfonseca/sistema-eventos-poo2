package br.ueg.eventos.desktop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import br.ueg.eventos.infrastructure.config.ApplicationEnvironment;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Small HTTP client shared by the JavaFX screens. */
final class ApiClient {
    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    ApiClient() {
        this(ApplicationEnvironment.getOrDefault("EVENTOS_API_URL", "http://localhost:8080"));
    }

    ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    JsonNode get(String path, Session session) throws IOException, InterruptedException {
        return request("GET", path, null, session);
    }

    JsonNode post(String path, Object body, Session session) throws IOException, InterruptedException {
        return request("POST", path, body, session);
    }

    JsonNode put(String path, Object body, Session session) throws IOException, InterruptedException {
        return request("PUT", path, body, session);
    }

    JsonNode delete(String path, Session session) throws IOException, InterruptedException {
        return request("DELETE", path, null, session);
    }

    byte[] getBytes(String path, Session session) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(20)).GET().header("Accept", "*/*");
        adicionarAutenticacao(builder, session);
        HttpResponse<byte[]> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 400) {
            throw new ApiException(response.statusCode(), new String(response.body(), StandardCharsets.UTF_8));
        }
        return response.body();
    }

    private JsonNode request(String method, String path, Object body, Session session)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json");
        adicionarAutenticacao(builder, session);
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        }
        HttpResponse<String> response = http.send(builder.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) throw new ApiException(response.statusCode(), response.body());
        if (response.body() == null || response.body().isBlank()) return NullNode.getInstance();
        return json.readTree(response.body());
    }

    private void adicionarAutenticacao(HttpRequest.Builder builder, Session session) {
        if (session == null) return;
        builder.header("Authorization", "Bearer " + session.token())
                .header("X-Usuario-Id", Integer.toString(session.id()));
    }

    record Session(int id, String nome, String email, String perfil, String token, String expiraEm) { }

    static final class ApiException extends IOException {
        private final int status;

        ApiException(int status, String message) {
            super(message == null || message.isBlank() ? "A API retornou HTTP " + status : message);
            this.status = status;
        }

        int status() { return status; }
    }
}
