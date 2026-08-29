package com.portfolio.studio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CmsTomcatMultipartSaveTests {

    private static final Path TEST_ROOT = createTempDirectory();
    private static final Path TEST_DB = TEST_ROOT.resolve("portfolio-tomcat-multipart.db");
    private static final Path TEST_UPLOADS = TEST_ROOT.resolve("uploads");
    private static final Pattern CSRF = Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");
    private static final String OWNER_USERNAME = "studio-owner";
    private static final String OWNER_PASSWORD = "ChangeMe123!";

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + TEST_DB.toString().replace("\\", "/"));
        registry.add("portfolio.storage.upload-root", () -> TEST_UPLOADS.toString().replace("\\", "/"));
        registry.add("portfolio.seed.owner.username", () -> OWNER_USERNAME);
        registry.add("portfolio.seed.owner.password", () -> OWNER_PASSWORD);
        registry.add("server.tomcat.max-part-count", () -> "100");
    }

    @AfterAll
    static void cleanup() throws IOException {
        if (Files.notExists(TEST_ROOT)) {
            return;
        }

        try (var walk = Files.walk(TEST_ROOT)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                        // Temporary test artifacts should not fail the suite cleanup.
                    }
                });
        }
    }

    @Test
    void savingFullProjectComposerFormDoesNotHitTomcatPartLimit() throws Exception {
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient client = HttpClient.newBuilder()
            .cookieHandler(cookies)
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        String loginPage = get(client, "/cmsmgmnt/sign-in").body();
        String loginCsrf = csrfToken(loginPage);

        HttpResponse<String> loginResponse = postForm(client, "/cmsmgmnt/sign-in", Map.of(
            "username", OWNER_USERNAME,
            "password", OWNER_PASSWORD,
            "_csrf", loginCsrf
        ));
        assertThat(loginResponse.statusCode()).isEqualTo(302);
        assertThat(sessionCookie(cookies)).isNotBlank();

        String composerPage = get(client, "/cmsmgmnt/projects/new").body();
        String saveCsrf = csrfToken(composerPage);

        MultipartBody multipart = new MultipartBody();
        multipart.addField("_csrf", saveCsrf);
        multipart.addField("id", "0");
        multipart.addField("title", "Tomcat part-count draft");
        multipart.addField("summary", "Enough fields to exceed Tomcat's default of 10 parts.");
        multipart.addField("narrative", "Saved through the real embedded Tomcat parser.");
        multipart.addEmptyFile("cardImageFile");
        multipart.addEmptyFile("galleryFiles");
        multipart.addField("yearLabel", "2026");
        multipart.addField("sortOrder", "1");
        multipart.addField("role", "Design");
        multipart.addField("tools", "Figma");
        multipart.addField("externalLink", "");
        multipart.addField("linkLabel", "");
        multipart.addField("cardGradient", "");
        multipart.addField("cardImageMode", "cover");
        multipart.addField("cardImageScale", "1");

        HttpResponse<String> saveResponse = client.send(
            HttpRequest.newBuilder(uri("/cmsmgmnt/projects/save"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", multipart.contentType())
                .POST(multipart.finish())
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );

        assertThat(saveResponse.body()).doesNotContain("FileCountLimitExceededException");
        assertThat(saveResponse.statusCode()).isEqualTo(302);
        assertThat(saveResponse.headers().firstValue("Location").orElse(""))
            .contains("/cmsmgmnt/projects");
    }

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        HttpResponse<String> response = client.send(
            HttpRequest.newBuilder(uri(path)).timeout(Duration.ofSeconds(15)).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    private HttpResponse<String> postForm(HttpClient client, String path, Map<String, String> fields) throws Exception {
        StringBuilder encoded = new StringBuilder();
        for (Map.Entry<String, String> field : fields.entrySet()) {
            if (!encoded.isEmpty()) {
                encoded.append('&');
            }
            encoded.append(URLEncoder.encode(field.getKey(), StandardCharsets.UTF_8))
                .append('=')
                .append(URLEncoder.encode(field.getValue(), StandardCharsets.UTF_8));
        }

        return client.send(
            HttpRequest.newBuilder(uri(path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encoded.toString()))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }

    private static String csrfToken(String html) {
        Matcher matcher = CSRF.matcher(html);
        assertThat(matcher.find()).as("CSRF token should be present in the page").isTrue();
        return matcher.group(1);
    }

    private static String sessionCookie(CookieManager cookies) {
        return cookies.getCookieStore().getCookies().stream()
            .map(HttpCookie::getName)
            .filter(name -> name.toUpperCase().contains("SESSION"))
            .findFirst()
            .orElse("");
    }

    private static Path createTempDirectory() {
        try {
            return Files.createTempDirectory("portfolio-tomcat-multipart-");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create temporary test directory.", exception);
        }
    }

    private static final class MultipartBody {
        private final String boundary = "----PortfolioTest" + UUID.randomUUID();
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        void addField(String name, String value) throws IOException {
            write("--" + boundary + "\r\n");
            write("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
            write(value);
            write("\r\n");
        }

        void addEmptyFile(String name) throws IOException {
            write("--" + boundary + "\r\n");
            write("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"\"\r\n");
            write("Content-Type: application/octet-stream\r\n\r\n");
            write("\r\n");
        }

        HttpRequest.BodyPublisher finish() throws IOException {
            write("--" + boundary + "--\r\n");
            return HttpRequest.BodyPublishers.ofByteArray(buffer.toByteArray());
        }

        String contentType() {
            return "multipart/form-data; boundary=" + boundary;
        }

        private void write(String text) throws IOException {
            buffer.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }
}
