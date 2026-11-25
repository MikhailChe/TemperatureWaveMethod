package thermophysics.backend;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
class Laboratory {
    static final int MAX_CHANNEL = 64;

    record Installation(UUID id, String name) {}
    record Configuration(UUID id, UUID installationId, int version, long effectiveFrom, JsonNode settings) {}
    record Calibration(UUID id, UUID configurationId, String name, String numberLocale, List<Integer> channels) {}
    record Channel(int column, boolean inverse) {}
    record Experiment(UUID id, UUID configurationId, String name, double lengthMeters, double density,
                      List<Channel> channels) {}
    record MeasurementFile(UUID id, UUID experimentId, long measuredAt, String fileName) {}
    record Run(UUID id, UUID experimentId, UUID configurationId, UUID calibrationId, long requestedAt, String status) {}
    record FileResult(UUID measurementId, String fileName, long measuredAt, JsonNode result, String error) {}
    record RunDetails(Run run, List<FileResult> measurements) {}
    private record CalibrationUpload(int column, String name, byte[] contents) {}

    private final JdbcClient jdbc;
    private final JsonMapper json;

    Laboratory(JdbcClient jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    Installation createInstallation(String name) {
        requireName(name);
        var installation = new Installation(UUID.randomUUID(), name);
        jdbc.sql("insert into installations values (?, ?)").params(installation.id(), name).update();
        return installation;
    }

    List<Installation> installations() {
        return jdbc.sql("select id, name from installations order by name, id").query(Installation.class).list();
    }

    @Transactional
    Configuration createConfiguration(UUID installationId, Long effectiveFrom, JsonNode settings) {
        require(effectiveFrom != null, "effectiveFrom is required");
        require(settings != null && settings.isObject(), "settings must be a JSON object");
        requireInstallation(installationId, true);
        int latest = jdbc.sql("select coalesce(max(version), 0) from configurations where installation_id = ?")
                .param(installationId).query(Integer.class).single();
        var configuration = new Configuration(UUID.randomUUID(), installationId, latest + 1, effectiveFrom, settings);
        jdbc.sql("insert into configurations values (?, ?, ?, ?, ?)")
                .params(configuration.id(), installationId, configuration.version(), effectiveFrom, json.writeValueAsString(settings))
                .update();
        return configuration;
    }

    List<Configuration> configurations(UUID installationId) {
        requireInstallation(installationId, false);
        return jdbc.sql("select * from configurations where installation_id = ? order by version")
                .param(installationId).query((rs, row) -> new Configuration(rs.getObject("id", UUID.class),
                        installationId, rs.getInt("version"), rs.getLong("effective_from"),
                        json.readTree(rs.getString("settings")))).list();
    }

    @Transactional
    Calibration createCalibration(UUID configurationId, String name, String numberLocale, List<Integer> columns,
                                  List<MultipartFile> files) throws IOException {
        requireConfiguration(configurationId);
        requireName(name);
        require(columns.size() == files.size() && !columns.isEmpty(), "Supply one channel per calibration file");
        require(new HashSet<>(columns).size() == columns.size(), "Calibration channels must be unique");
        Locale locale = Locale.forLanguageTag(numberLocale);
        require(!locale.getLanguage().isEmpty() && locale.toLanguageTag().length() <= 80, "numberLocale must be a language tag, for example ru-RU");
        var uploads = new ArrayList<CalibrationUpload>(files.size());
        for (int i = 0; i < files.size(); i++) {
            int column = columns.get(i);
            requireChannel(column);
            MultipartFile file = files.get(i);
            byte[] contents = contents(file);
            try {
                CalculationFiles.loadCalibration(contents, locale);
            } catch (IOException e) {
                throw problem(HttpStatus.UNPROCESSABLE_CONTENT, "Invalid phase calibration for channel " + column);
            }
            uploads.add(new CalibrationUpload(column, fileName(file), contents));
        }

        UUID id = UUID.randomUUID();
        jdbc.sql("insert into calibrations values (?, ?, ?, ?)").params(id, configurationId, name, locale.toLanguageTag()).update();
        for (CalibrationUpload upload : uploads) {
            jdbc.sql("insert into calibration_files values (?, ?, ?, ?)")
                    .params(id, upload.column(), upload.name(), upload.contents()).update();
        }
        return new Calibration(id, configurationId, name, locale.toLanguageTag(), columns.stream().sorted().toList());
    }

    @Transactional(readOnly = true)
    List<Calibration> calibrations(UUID configurationId) {
        requireConfiguration(configurationId);
        return jdbc.sql("select id, name, number_locale from calibrations where configuration_id = ? order by name, id")
                .param(configurationId).query((rs, row) -> {
                    UUID id = rs.getObject("id", UUID.class);
                    return new Calibration(id, configurationId, rs.getString("name"), rs.getString("number_locale"),
                            calibrationColumns(id));
                }).list();
    }

    Experiment createExperiment(UUID configurationId, String name, double lengthMeters, double density, List<Channel> channels) {
        requireConfiguration(configurationId);
        requireName(name);
        require(Double.isFinite(lengthMeters) && lengthMeters > 0, "lengthMeters must be positive and finite");
        require(Double.isFinite(density) && density > 0, "density must be positive and finite");
        require(channels != null && !channels.isEmpty(), "At least one measurement channel is required");
        Set<Integer> columns = new HashSet<>();
        for (Channel channel : channels) {
            require(channel != null, "A channel cannot be null");
            requireChannel(channel.column());
            require(columns.add(channel.column()), "Experiment channels must be unique");
        }
        var experiment = new Experiment(UUID.randomUUID(), configurationId, name, lengthMeters, density, channels);
        jdbc.sql("insert into experiments values (?, ?, ?, ?, ?, ?)")
                .params(experiment.id(), configurationId, name, lengthMeters, density, json.writeValueAsString(channels)).update();
        return experiment;
    }

    List<Experiment> experiments(UUID configurationId) {
        requireConfiguration(configurationId);
        return jdbc.sql("select * from experiments where configuration_id = ? order by name, id")
                .param(configurationId).query(this::readExperiment).list();
    }

    MeasurementFile addMeasurement(UUID experimentId, long measuredAt, MultipartFile file) throws IOException {
        experiment(experimentId);
        var measurement = new MeasurementFile(UUID.randomUUID(), experimentId, measuredAt, fileName(file));
        jdbc.sql("insert into measurements values (?, ?, ?, ?, ?)")
                .params(measurement.id(), experimentId, measuredAt, measurement.fileName(), contents(file)).update();
        return measurement;
    }

    List<MeasurementFile> measurements(UUID experimentId) {
        experiment(experimentId);
        return jdbc.sql("select id, experiment_id, measured_at, file_name from measurements where experiment_id = ? order by measured_at, id")
                .param(experimentId).query(MeasurementFile.class).list();
    }

    @Transactional
    Run queueRun(UUID experimentId, UUID calibrationId) {
        require(calibrationId != null, "calibrationId is required");
        Experiment experiment = experiment(experimentId);
        UUID configurationId = jdbc.sql("select configuration_id from calibrations where id = ?")
                .param(calibrationId).query(UUID.class).optional()
                .orElseThrow(() -> problem(HttpStatus.NOT_FOUND, "Calibration not found"));
        if (!experiment.configurationId().equals(configurationId)) {
            throw problem(HttpStatus.CONFLICT, "Calibration belongs to another configuration version");
        }
        Set<Integer> calibrated = new HashSet<>(calibrationColumns(calibrationId));
        for (Channel channel : experiment.channels()) {
            if (!calibrated.contains(channel.column())) {
                throw problem(HttpStatus.CONFLICT, "Calibration is missing channel " + channel.column());
            }
        }
        var run = new Run(UUID.randomUUID(), experimentId, configurationId, calibrationId, System.currentTimeMillis(), "QUEUED");
        jdbc.sql("insert into calculation_runs (id, experiment_id, configuration_id, calibration_id, requested_at, status) values (?, ?, ?, ?, ?, ?)")
                .params(run.id(), experimentId, configurationId, calibrationId, run.requestedAt(), run.status()).update();
        int count = jdbc.sql("""
                insert into run_measurements (run_id, experiment_id, measurement_id)
                select ?, experiment_id, id from measurements where experiment_id = ?
                """).params(run.id(), experimentId).update();
        if (count == 0) {
            throw problem(HttpStatus.CONFLICT, "Upload at least one measurement before starting a calculation");
        }
        return run;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    RunDetails runDetails(UUID runId) {
        Run run = jdbc.sql("select id, experiment_id, configuration_id, calibration_id, requested_at, status from calculation_runs where id = ?")
                .param(runId).query(Run.class).optional().orElseThrow(() -> problem(HttpStatus.NOT_FOUND, "Calculation not found"));
        List<FileResult> results = jdbc.sql("""
                select m.id, m.file_name, m.measured_at, rm.result, rm.error_message
                from run_measurements rm join measurements m on m.id = rm.measurement_id
                where rm.run_id = ? order by m.measured_at, m.id
                """).param(runId).query((rs, row) -> {
                    String result = rs.getString("result");
                    return new FileResult(rs.getObject("id", UUID.class), rs.getString("file_name"),
                            rs.getLong("measured_at"), result == null ? null : json.readTree(result), rs.getString("error_message"));
                }).list();
        return new RunDetails(run, results);
    }

    List<Run> runs(UUID experimentId) {
        experiment(experimentId);
        return jdbc.sql("""
                select id, experiment_id, configuration_id, calibration_id, requested_at, status
                from calculation_runs where experiment_id = ? order by requested_at, id
                """).param(experimentId).query(Run.class).list();
    }

    Experiment experiment(UUID id) {
        return jdbc.sql("select * from experiments where id = ?").param(id).query(this::readExperiment)
                .optional().orElseThrow(() -> problem(HttpStatus.NOT_FOUND, "Experiment not found"));
    }

    private Experiment readExperiment(ResultSet rs, int row) throws SQLException {
        return new Experiment(rs.getObject("id", UUID.class), rs.getObject("configuration_id", UUID.class),
                rs.getString("name"), rs.getDouble("length_meters"), rs.getDouble("density"),
                List.of(json.readValue(rs.getString("channels"), Channel[].class)));
    }

    private List<Integer> calibrationColumns(UUID calibrationId) {
        return jdbc.sql("select channel from calibration_files where calibration_id = ? order by channel")
                .param(calibrationId).query(Integer.class).list();
    }

    private void requireInstallation(UUID id, boolean lock) {
        String sql = "select id from installations where id = ?";
        jdbc.sql(lock ? sql + " for update" : sql).param(id).query(UUID.class).optional()
                .orElseThrow(() -> problem(HttpStatus.NOT_FOUND, "Installation not found"));
    }

    private void requireConfiguration(UUID configurationId) {
        jdbc.sql("select id from configurations where id = ?").param(configurationId).query(UUID.class).optional()
                .orElseThrow(() -> problem(HttpStatus.NOT_FOUND, "Configuration not found"));
    }

    private static void requireChannel(int column) {
        require(column >= 1 && column <= MAX_CHANNEL, "Channel must be between 1 and " + MAX_CHANNEL);
    }

    private static void requireName(String name) {
        require(name != null && !name.isBlank() && name.length() <= 200, "name must contain between 1 and 200 characters");
    }

    private static byte[] contents(MultipartFile file) throws IOException {
        require(!file.isEmpty(), "File cannot be empty");
        return file.getBytes();
    }

    private static String fileName(MultipartFile file) {
        String name = file.getOriginalFilename();
        require(name != null && !name.isBlank() && name.length() <= 255, "Invalid file name");
        return name;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw problem(HttpStatus.BAD_REQUEST, message);
        }
    }

    private static ResponseStatusException problem(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }
}
