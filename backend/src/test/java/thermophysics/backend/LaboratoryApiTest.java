package thermophysics.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "twm.calculation.poll-delay-ms=25"
})
class LaboratoryApiTest {
    @LocalServerPort
    int port;

    @Autowired
    JsonMapper json;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> System.getenv().getOrDefault("TWM_TEST_DATABASE_URL",
                "jdbc:h2:mem:laboratory;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"));
    }

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void reusesCalibrationForTwoExperimentsAndEnforcesConfigurationVersion() throws Exception {
        String installation = post("/installations", """
                {"name":"Laboratory installation"}
                """, 201).get("id").asString();
        String configurations = "/installations/" + installation + "/configurations";
        JsonNode v1 = post(configurations, """
                {"effectiveFrom":1000,"settings":{"gain":270}}
                """, 201);
        String config1 = v1.get("id").asString();
        String calibration = uploadCalibration(config1, "0,5\t7,5\n1,5\t12,5\n", 201).get("id").asString();
        String first = createExperiment(config1, 0.002);
        String second = createExperiment(config1, 0.004);
        uploadMeasurement(first, 1111, "first.txt", measurement());
        uploadMeasurement(second, 1222, "second.txt", measurement());

        String run1 = startRun(first, calibration, 202).get("id").asString();
        String run2 = startRun(second, calibration, 202).get("id").asString();
        uploadMeasurement(second, 1333, "extra.txt", "2\t10\n0\t100\n0\t100\n");
        String partialRun = startRun(second, calibration, 202).get("id").asString();

        JsonNode v2 = post(configurations, """
                {"effectiveFrom":2000,"settings":{"gain":540}}
                """, 201);
        String config2 = v2.get("id").asString();
        assertThat(v1.get("version").asInt()).isEqualTo(1);
        assertThat(v2.get("version").asInt()).isEqualTo(2);
        assertThat(get(configurations).size()).isEqualTo(2);
        assertThat(get("/configurations/" + config1 + "/calibrations").get(0).get("id").asString())
                .isEqualTo(calibration);
        String late = createExperiment(config1, 0.002);
        uploadMeasurement(late, 1444, "late.txt", measurement());
        String lateRun = startRun(late, calibration, 202).get("id").asString();

        String third = createExperiment(config2, 0.002);
        uploadMeasurement(third, 2222, "third.txt", measurement());
        startRun(third, calibration, 409);
        uploadCalibration(config2, "bad data", 422);
        assertThat(get("/configurations/" + config2 + "/calibrations").size()).isZero();
        String newCalibration = uploadCalibration(config2, "1,0\t20,0\n", 201).get("id").asString();
        String run3 = startRun(third, newCalibration, 202).get("id").asString();

        double expected = Math.PI * 0.002 * 0.002 / Math.pow(Math.toRadians(35), 2);
        assertResult(awaitRun(run1, "COMPLETED"), calibration, 1111, expected);
        assertResult(awaitRun(run2, "COMPLETED"), calibration, 1222, expected * 4);
        assertResult(awaitRun(lateRun, "COMPLETED"), calibration, 1444, expected);
        assertResult(awaitRun(run3, "COMPLETED"), newCalibration, 2222,
                Math.PI * 0.002 * 0.002 / Math.pow(Math.toRadians(25), 2));
        JsonNode partial = awaitRun(partialRun, "PARTIAL").get("measurements");
        assertThat(partial.size()).isEqualTo(2);
        assertThat(partial.get(0).get("result").isObject()).isTrue();
        assertThat(partial.get(1).get("error").asString()).contains("reference channel");
    }

    private void assertResult(JsonNode response, String calibration, long measuredAt, double expected) {
        assertThat(response.get("run").get("calibrationId").asString()).isEqualTo(calibration);
        JsonNode files = response.get("measurements");
        assertThat(files.size()).isEqualTo(1);
        assertThat(files.get(0).get("measuredAt").asLong()).isEqualTo(measuredAt);
        assertThat(files.get(0).at("/result/channels/0/diffusivity").asDouble()).isCloseTo(expected, within(1e-12));
    }

    private String createExperiment(String configuration, double length) throws Exception {
        return post("/configurations/" + configuration + "/experiments", """
                {"name":"Sample","lengthMeters":%s,"density":8000,"channels":[{"column":1,"inverse":false}]}
                """.formatted(length), 201).get("id").asString();
    }

    private JsonNode uploadCalibration(String configuration, String data, int status) throws Exception {
        return multipart("/configurations/" + configuration + "/calibrations", "phase.zc", data, status,
                "name", "Phase calibration", "numberLocale", "ru-RU", "channel", "1");
    }

    private void uploadMeasurement(String experiment, long measuredAt, String name, String data) throws Exception {
        multipart("/experiments/" + experiment + "/measurements", name, data, 201,
                "measuredAt", Long.toString(measuredAt));
    }

    private JsonNode startRun(String experiment, String calibration, int status) throws Exception {
        return post("/experiments/" + experiment + "/runs", """
                {"calibrationId":"%s"}
                """.formatted(calibration), status);
    }

    private JsonNode awaitRun(String run, String expectedStatus) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        JsonNode response;
        do {
            response = get("/runs/" + run);
            if (!response.get("run").get("status").asString().equals("QUEUED")) {
                break;
            }
            Thread.sleep(25);
        } while (System.nanoTime() < deadline);
        assertThat(response.get("run").get("status").asString()).as(response.toString()).isEqualTo(expectedStatus);
        return response;
    }

    private JsonNode multipart(String path, String filename, String data, int status, String... fields) throws Exception {
        String boundary = UUID.randomUUID().toString();
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < fields.length; i += 2) {
            body.append("--").append(boundary).append("\r\nContent-Disposition: form-data; name=\"")
                    .append(fields[i]).append("\"\r\n\r\n").append(fields[i + 1]).append("\r\n");
        }
        body.append("--").append(boundary).append("\r\nContent-Disposition: form-data; name=\"file\"; filename=\"")
                .append(filename).append("\"\r\nContent-Type: text/plain\r\n\r\n").append(data)
                .append("\r\n--").append(boundary).append("--\r\n");
        return send(request(path).header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build(), status);
    }

    private JsonNode post(String path, String body, int status) throws Exception {
        return send(request(path).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), status);
    }

    private JsonNode get(String path) throws Exception {
        return send(request(path).GET().build(), 200);
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api" + path))
                .timeout(Duration.ofSeconds(10));
    }

    private JsonNode send(HttpRequest request, int status) throws Exception {
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as("%s %s", request.uri(), response.body()).isEqualTo(status);
        return json.readTree(response.body());
    }

    private static String measurement() {
        int period = 1024;
        int rows = period * 3;
        StringBuilder data = new StringBuilder().append(rows).append("\t10\n");
        for (int row = 0; row < rows; row++) {
            double angle = 2 * Math.PI * (row - 128) / period;
            data.append(row % period < 128 ? -10 : -1).append('\t')
                    .append(Math.round(-1000 * Math.cos(angle))).append('\n');
        }
        return data.toString();
    }
}
